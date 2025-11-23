package com.bbey.neez.service.Company;

import com.bbey.neez.entity.Company;
import com.bbey.neez.repository.CompanyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 0. corpCode.csv에서 기초 회사 목록 로딩 (DART corpCode)
 * 1. 명함에서 추출한 회사명으로 BizNo API에 상호 검색 → 후보 리스트
 * 2. 각 후보에 대해 금융위원회 기업기본정보(기업개황) API 호출 → 회사 정보 보강
 * 3. 회사명 + 주소 유사도 기반으로 최적의 회사 1개 선택
 * 4. companies 테이블에 저장(이미 있으면 재사용) 후 Company 엔티티 반환
 */
@Service
public class CompanyInfoExtractServiceImpl implements CompanyInfoExtractService {

    private final CompanyRepository companyRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // BizNo API (bizno.* 사용)
    @Value("${bizno.api-key:}")
    private String bizApiServiceKey;

    @Value("${bizno.api-url:}")
    private String bizApiUrl;

    // FSS (fss.* 사용)
    @Value("${fss.api-url:}")
    private String fssCorpInfoUrl;

    @Value("${fss.service-key:}")
    private String fssApiKey;

    // corpCode.csv 경로 (dart.* 사용)
    @Value("${dart.corp-code-csv-path:}")
    private String corpCodeCsvPath;

    private final List<DartCorpCode> dartCorpCodes = new ArrayList<>();

    public CompanyInfoExtractServiceImpl(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
        loadDartCorpCodes();
    }

    // =================== Public API ===================

    /**
     * 외부 API까지 사용하는 "무거운" 회사 정보 추출 & 저장 메서드
     */
    @Override
    public Optional<Company> extractAndSave(String companyName, String address) {
        if (isEmpty(companyName))
            return Optional.empty();

        // 1. BizNo API로 후보 조회
        List<BizNoCandidate> candidates = callBizNoAndParse(companyName);
        if (candidates.isEmpty())
            return Optional.empty();

        // 2. 각 후보에 대해 금융위 정보 보강
        for (BizNoCandidate c : candidates) {
            if (isEmpty(c.cno))
                continue;
            String crnoDigits = normalizeNumber(c.cno);
            String fssXml = callFssByCrno(crnoDigits);
            FssCorpInfo info = parseFssInfo(fssXml);
            if (info != null) {
                c.fssCorpName = nvl(info.corpNm);
                c.fssRepName = nvl(info.enpRprFnm);
                c.fssBizNo = normalizeNumber(info.bzno);
                c.fssAddress = nvl(info.enpBsadr);
                c.fssHomepage = nvl(info.enpHmpgUrl);
            }
        }

        // 3. 후보 필터링
        List<BizNoCandidate> filtered = filterCandidates(candidates);

        // 4. 이름/주소 유사도 점수 기반 최종 후보 선택
        Optional<MatchedCompany> matchedOpt = matchCompany(companyName, address, filtered);
        if (!matchedOpt.isPresent()) {
            System.out.println("[extractAndSave] 최종 매칭 실패: companyName=" + companyName + ", address=" + address);
            return Optional.empty();
        }

        MatchedCompany matched = matchedOpt.get();
        String bizNoDigits = coalesce(matched.candidate.fssBizNo, normalizeNumber(matched.candidate.bno));
        String corpNoDigits = normalizeNumber(matched.candidate.cno);

        // 5. DB에서 기존 회사 찾기 (bizNo / corpNo 기반)
        Optional<Company> byBizNo = isEmpty(bizNoDigits)
                ? Optional.empty()
                : companyRepository.findByBizNo(bizNoDigits);

        Optional<Company> byCorpNo = isEmpty(corpNoDigits)
                ? Optional.empty()
                : companyRepository.findByCorpNo(corpNoDigits);

        Company company = byBizNo.orElseGet(() -> byCorpNo.orElseGet(Company::new));

        // 6. 매칭 결과로 Company 필드 채우기
        fillCompanyFromMatched(company, matched, bizNoDigits, corpNoDigits);

        Company saved = companyRepository.save(company);
        return Optional.of(saved);
    }

    /**
     * 외부 API 없이 DB 기반으로만 회사 정보를 매칭/생성하는 "가벼운" 메서드
     */
    @Override
    public Optional<Company> matchOrCreateCompany(String name, String address) {
        if (isEmpty(name))
            return Optional.empty();

        // 1) name + address 로 정확히 일치하는 회사 우선
        if (!isEmpty(address)) {
            Optional<Company> existedExact = companyRepository.findFirstByNameAndAddress(name, address);
            if (existedExact.isPresent())
                return existedExact;
        }

        // 2) name 만으로 검색
        Optional<Company> existedByName = companyRepository.findByName(name);
        if (existedByName.isPresent())
            return existedByName;

        // 3) 그래도 없으면 새로 생성
        Company company = new Company();
        company.setName(name);
        if (!isEmpty(address)) {
            company.setAddress(address);
        }
        company.setCreatedAt(LocalDateTime.now());
        company.setUpdatedAt(LocalDateTime.now());

        Company saved = companyRepository.save(company);
        return Optional.of(saved);
    }

    // =================== corpCode.csv 로딩 ===================

    private void loadDartCorpCodes() {
        try {
            // 🔹 설정이 없으면 바로 종료 (NPE 방지)
            if (corpCodeCsvPath == null || corpCodeCsvPath.trim().isEmpty()) {
                System.out.println("[loadDartCorpCodes] dart.corp-code-csv-path 설정 없음. corpCode.csv 로딩 스킵");
                return;
            }

            Path path = Paths.get(corpCodeCsvPath);
            if (!Files.exists(path)) {
                System.out.println("[loadDartCorpCodes] corpCode.csv 파일이 존재하지 않습니다: " + corpCodeCsvPath);
                return;
            }

            List<String> lines = Files.readAllLines(path, Charset.forName("UTF-8"));
            for (int i = 1; i < lines.size(); i++) { // 첫 줄은 헤더
                String line = lines.get(i);
                String[] parts = line.split(",", -1);
                if (parts.length < 4)
                    continue;

                DartCorpCode c = new DartCorpCode();
                c.corpCode = parts[0].replaceAll("\"", "").trim();
                c.corpName = parts[1].replaceAll("\"", "").trim();
                c.stockCode = parts[2].replaceAll("\"", "").trim();
                c.modifyDate = parts[3].replaceAll("\"", "").trim();

                dartCorpCodes.add(c);
            }

            System.out.println("[loadDartCorpCodes] corpCode.csv 로딩 완료. 총 " + dartCorpCodes.size() + "건");
        } catch (IOException e) {
            System.out.println("[loadDartCorpCodes] corpCode.csv 로딩 실패: " + e.getMessage());
        }
    }

    // =================== BizNo 호출 ===================

    private List<BizNoCandidate> callBizNoAndParse(String companyName) {
        List<BizNoCandidate> result = new ArrayList<>();

        // 키/URL 없으면 스킵
        if (bizApiServiceKey == null || bizApiServiceKey.trim().isEmpty()
                || bizApiUrl == null || bizApiUrl.trim().isEmpty()) {
            System.out.println("[callBizNoAndParse] bizno.api-key 또는 bizno.api-url 설정 없음. BizNo API 호출 스킵");
            return result;
        }

        try {
            String encodedName = URLEncoder.encode(companyName, "UTF-8");
            // ✅ yml의 bizno.api-url 사용
            String urlStr = bizApiUrl + "?key=" + bizApiServiceKey + "&q=" + encodedName;

            String json = httpGet(urlStr);
            if (isEmpty(json)) {
                System.out.println("[callBizNoAndParse] BizNo 응답이 비어 있습니다");
                return result;
            }

            Map<?, ?> root = objectMapper.readValue(json, Map.class);
            Object dataObj = root.get("data");
            if (!(dataObj instanceof List))
                return result;

            List<?> lst = (List<?>) dataObj;
            for (Object o : lst) {
                if (!(o instanceof Map))
                    continue;
                Map<?, ?> m = (Map<?, ?>) o;
                BizNoCandidate c = new BizNoCandidate();
                c.company = nvl((String) m.get("company"));
                c.bno = nvl((String) m.get("bno"));
                c.cno = nvl((String) m.get("cno"));
                result.add(c);
            }

            System.out.println("[callBizNoAndParse] BizNo 후보 수=" + result.size());

        } catch (Exception e) {
            System.out.println("[callBizNoAndParse] 예외 발생: " + e.getMessage());
        }

        return result;
    }

    // =================== 금융위 기업개요조회 ===================
    private String callFssByCrno(String crnoDigits) {
        if (isEmpty(crnoDigits))
            return null;

        // URL/키 없으면 스킵
        if (fssCorpInfoUrl == null || fssCorpInfoUrl.trim().isEmpty()
                || fssApiKey == null || fssApiKey.trim().isEmpty()) {
            System.out.println("[callFssByCrno] fss.api-url 또는 fss.service-key 설정 없음. FSS API 호출 스킵");
            return null;
        }

        try {
            String query = "crno=" + URLEncoder.encode(crnoDigits, "UTF-8")
                    + "&serviceKey=" + URLEncoder.encode(fssApiKey, "UTF-8");

            String urlStr = fssCorpInfoUrl + "?" + query;
            return httpGet(urlStr);
        } catch (Exception e) {
            System.out.println("[callFssByCrno] 예외 발생: " + e.getMessage());
            return null;
        }
    }

    private FssCorpInfo parseFssInfo(String xml) {
        if (isEmpty(xml))
            return null;

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            factory.setIgnoringComments(true);
            factory.setIgnoringElementContentWhitespace(true);
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.parse(new InputSource(new StringReader(xml)));
            doc.getDocumentElement().normalize();

            NodeList list = doc.getElementsByTagName("list");
            if (list.getLength() == 0)
                return null;

            Node item = list.item(0);
            if (item.getNodeType() != Node.ELEMENT_NODE)
                return null;
            Element e = (Element) item;

            FssCorpInfo info = new FssCorpInfo();
            info.corpNm = getChildText(e, "corpNm");
            info.enpRprFnm = getChildText(e, "enpRprFnm");
            info.bzno = getChildText(e, "bzno");
            info.enpBsadr = getChildText(e, "enpBsadr");
            info.enpHmpgUrl = getChildText(e, "enpHmpgUrl");

            return info;
        } catch (Exception e) {
            System.out.println("[parseFssInfo] 예외 발생: " + e.getMessage());
            return null;
        }
    }

    // =================== 필터링 및 매칭 로직 ===================

    private List<BizNoCandidate> filterCandidates(List<BizNoCandidate> candidates) {
        List<BizNoCandidate> filtered = new ArrayList<>();
        for (BizNoCandidate c : candidates) {
            if (!isEmpty(c.cno)) {
                filtered.add(c);
            }
        }
        if (filtered.isEmpty()) {
            filtered = candidates;
        }
        return filtered;
    }

    private Optional<MatchedCompany> matchCompany(
            String cardCompanyName,
            String cardAddress,
            List<BizNoCandidate> candidates) {

        if (isEmpty(cardCompanyName)) {
            System.out.println("[matchCompany] 회사명이 없어 매칭 불가");
            return Optional.empty();
        }

        if (isEmpty(cardAddress)) {
            System.out.println("[matchCompany] 명함 주소가 없어 매칭 불가");
            return Optional.empty();
        }

        String normCardName = normalizeCompanyName(cardCompanyName);
        String normCardAddr = normalizeAddress(cardAddress);

        // 주소가 어느 정도 맞는 후보만 우선 필터링
        List<BizNoCandidate> exactMatches = new ArrayList<>();
        for (BizNoCandidate c : candidates) {
            if (isEmpty(c.fssAddress))
                continue;
            String normAddress = normalizeAddress(c.fssAddress);
            if (normAddress.contains(normCardAddr) || normCardAddr.contains(normAddress)) {
                exactMatches.add(c);
            }
        }

        List<MatchedCompany> scoredList = new ArrayList<>();
        List<BizNoCandidate> baseList = exactMatches.isEmpty() ? candidates : exactMatches;

        for (BizNoCandidate c : baseList) {
            String normCandName = normalizeCompanyName(!isEmpty(c.fssCorpName) ? c.fssCorpName : c.company);

            int score = calcNameSimilarityScore(normCardName, normCandName);

            if (!isEmpty(cardAddress) && !isEmpty(c.fssAddress)) {
                score += calcAddressSimilarityScore(cardAddress, c.fssAddress);
            }

            if (score > 0) {
                scoredList.add(new MatchedCompany(c, score));
            }
        }

        if (scoredList.isEmpty()) {
            System.out.println("[matchCompany] 이름+주소 기준으로 점수 매긴 후보가 없음 → 매칭 불가");
            return Optional.empty();
        }

        scoredList.sort((a, b) -> Integer.compare(b.score, a.score));

        MatchedCompany best = scoredList.get(0);
        if (scoredList.size() > 1) {
            MatchedCompany second = scoredList.get(1);
            int diff = best.score - second.score;
            if (diff < 10) {
                System.out.println("[matchCompany] 상위 2개 점수 차이가 작아 확정 불가 → 매칭 포기");
                System.out.println("=== 1st Candidate ===");
                printCandidateDetail(best);
                System.out.println("=== 2nd Candidate ===");
                printCandidateDetail(second);
                return Optional.empty();
            }
        }

        System.out.println("[matchCompany] 최종 매칭 성공, score=" + best.score);
        printCandidateDetail(best);
        return Optional.of(best);
    }

    private void fillCompanyFromMatched(Company company,
            MatchedCompany mc,
            String bizNoDigits,
            String corpNoDigits) {
        BizNoCandidate c = mc.candidate;

        // 회사명
        if (company.getName() == null || company.getName().isEmpty()) {
            company.setName(coalesce(c.fssCorpName, c.company));
        }

        // 대표자 이름 (repName 사용)
        if (company.getRepName() == null || company.getRepName().isEmpty()) {
            company.setRepName(nvl(c.fssRepName));
        }

        // 사업자번호
        if (!isEmpty(bizNoDigits)) {
            company.setBizNo(bizNoDigits);
        }

        // 법인번호
        if (!isEmpty(corpNoDigits)) {
            company.setCorpNo(corpNoDigits);
        }

        // 주소
        if (company.getAddress() == null || company.getAddress().isEmpty()) {
            company.setAddress(nvl(c.fssAddress));
        }

        // 홈페이지
        if (company.getHomepage() == null || company.getHomepage().isEmpty()) {
            company.setHomepage(nvl(c.fssHomepage));
        }

        if (company.getCreatedAt() == null) {
            company.setCreatedAt(LocalDateTime.now());
        }
        company.setUpdatedAt(LocalDateTime.now());
    }

    // =================== HTTP 유틸 ===================

    private String httpGet(String urlStr) throws IOException {
        HttpURLConnection conn = null;
        BufferedReader rd = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(7000);

            int responseCode = conn.getResponseCode();
            InputStream is = (200 <= responseCode && responseCode <= 299)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            rd = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = rd.readLine()) != null) {
                sb.append(line);
            }

            return sb.toString();
        } finally {
            if (rd != null)
                rd.close();
            if (conn != null)
                conn.disconnect();
        }
    }

    // =================== 문자열 유틸 ===================

    private boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    private String nvl(String s) {
        return s == null ? "" : s;
    }

    private String normalizeNumber(String s) {
        if (s == null)
            return null;
        String digits = s.replaceAll("[^0-9]", "");
        return digits.isEmpty() ? null : digits;
    }

    private String normalizeCompanyName(String name) {
        if (name == null)
            return "";
        String n = name;
        n = n.replaceAll("\\(주\\)", "");
        n = n.replaceAll("주식회사", "");
        n = n.replaceAll("주\\.", "");
        n = n.replaceAll("\\s+", "");
        return n.toLowerCase(Locale.ROOT);
    }

    private String normalizeAddress(String addr) {
        if (addr == null)
            return "";
        return addr.replaceAll("\\s+", "").toLowerCase(Locale.ROOT);
    }

    private int calcNameSimilarityScore(String base, String target) {
        if (isEmpty(base) || isEmpty(target))
            return 0;
        int score = 0;
        if (target.contains(base) || base.contains(target)) {
            score += 50;
        }
        int minLen = Math.min(base.length(), target.length());
        int common = 0;
        for (int i = 0; i < minLen; i++) {
            if (base.charAt(i) == target.charAt(i))
                common++;
        }
        score += common * 2;
        return score;
    }

    private int calcAddressSimilarityScore(String cardAddress, String candAddress) {
        if (isEmpty(cardAddress) || isEmpty(candAddress))
            return 0;
        String ca = normalizeAddress(cardAddress);
        String ta = normalizeAddress(candAddress);
        int score = 0;
        if (ta.contains(ca) || ca.contains(ta)) {
            score += 30;
        }
        int minLen = Math.min(ca.length(), ta.length());
        int common = 0;
        for (int i = 0; i < minLen; i++) {
            if (ca.charAt(i) == ta.charAt(i))
                common++;
        }
        score += common;
        return score;
    }

    private String getChildText(Element parent, String tag) {
        NodeList list = parent.getElementsByTagName(tag);
        if (list.getLength() == 0)
            return "";
        Node n = list.item(0);
        return n.getTextContent();
    }

    private String coalesce(String a, String b) {
        if (a != null && !a.isEmpty())
            return a;
        return b;
    }

    // =================== 내부 DTO ===================

    private static class BizNoCandidate {
        String company;
        String bno;
        String cno;

        String fssCorpName;
        String fssRepName;
        String fssBizNo;
        String fssAddress;
        String fssHomepage;
    }

    private static class DartCorpCode {
        String corpCode;
        String corpName;
        String stockCode;
        String modifyDate;
    }

    private static class FssCorpInfo {
        String corpNm;
        String enpRprFnm;
        String bzno;
        String enpBsadr;
        String enpHmpgUrl;
    }

    private static class MatchedCompany {
        BizNoCandidate candidate;
        int score;

        MatchedCompany(BizNoCandidate c, int score) {
            this.candidate = c;
            this.score = score;
        }
    }

    private void printCandidateDetail(MatchedCompany mc) {
        BizNoCandidate c = mc.candidate;
        System.out.println("score        : " + mc.score);
        System.out.println("company(Biz) : " + c.company);
        System.out.println("corpName(FSS): " + c.fssCorpName);
        System.out.println("repName(FSS) : " + c.fssRepName);
        System.out.println("bizNo        : " + coalesce(c.fssBizNo, normalizeNumber(c.bno)));
        System.out.println("corpNo(cno)  : " + normalizeNumber(c.cno));
        System.out.println("address      : " + c.fssAddress);
        System.out.println("homepage     : " + c.fssHomepage);
        System.out.println("---------------------------------------------");
    }
}
