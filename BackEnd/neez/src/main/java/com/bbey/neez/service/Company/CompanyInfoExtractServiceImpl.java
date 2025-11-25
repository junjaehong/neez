package com.bbey.neez.service.Company;

import com.bbey.neez.entity.Company;
import com.bbey.neez.repository.CompanyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.annotation.PostConstruct;
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
import java.nio.charset.StandardCharsets;

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

    // 🔹 생성자에서는 로딩 X — 단순히 Repository만 주입
    public CompanyInfoExtractServiceImpl(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    // 🔹 @Value 주입이 끝난 뒤에 호출됨
    @PostConstruct
    public void init() {
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

        // 🔹 여기부터 추가: BizNo/FSS 매칭 실패 시 DART corpCode.csv로 재시도
        if (!matchedOpt.isPresent()) {
            System.out.println("[extractAndSave] BizNo/FSS 기준 최종 매칭 실패 → DART corpCode.csv로 재시도");
            Optional<Company> dartMatched = matchFromDartAndSave(companyName, address);
            if (dartMatched.isPresent()) {
                return dartMatched;
            }
            System.out.println("[extractAndSave] DART 매칭도 실패 → Optional.empty 반환");
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
            System.out.println("[loadDartCorpCodes] corpCodeCsvPath raw = '" + corpCodeCsvPath + "'");

            if (corpCodeCsvPath == null || corpCodeCsvPath.trim().isEmpty()) {
                System.out.println("[loadDartCorpCodes] dart.corp-code-csv-path 설정 없음. corpCode.csv 로딩 스킵");
                return;
            }

            List<String> lines;

            // 🔹 classpath:로 시작하면 리소스로 읽기
            if (corpCodeCsvPath.startsWith("classpath:")) {
                String cp = corpCodeCsvPath.substring("classpath:".length()); // "corpCode.csv"

                InputStream is = getClass().getClassLoader().getResourceAsStream(cp);
                if (is == null) {
                    System.out.println("[loadDartCorpCodes] classpath 리소스를 찾을 수 없습니다: " + cp);
                    return;
                }

                try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    lines = new ArrayList<>();
                    String line;
                    while ((line = br.readLine()) != null) {
                        lines.add(line);
                    }
                }
            } else {
                // 🔹 그 외에는 그냥 파일 경로로 취급
                Path path = Paths.get(corpCodeCsvPath);
                if (!Files.exists(path)) {
                    System.out.println("[loadDartCorpCodes] corpCode.csv 파일이 존재하지 않습니다: " + corpCodeCsvPath);
                    return;
                }
                lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            }

            if (lines.size() <= 1) {
                System.out.println("[loadDartCorpCodes] corpCode.csv 내용이 비어있거나 헤더만 있습니다.");
                return;
            }

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
        } catch (Exception e) {
            System.out.println("[loadDartCorpCodes] 예외 발생: " + e.getMessage());
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
            String urlStr = bizApiUrl + "?key=" + bizApiServiceKey + "&type=json" + "&q=" + encodedName;

            String json = httpGet(urlStr);
            if (isEmpty(json)) {
                System.out.println("[callBizNoAndParse] BizNo 응답이 비어 있습니다");
                return result;
            }
            System.out.println("[BizCard raw] " + json);

            Map<?, ?> root = objectMapper.readValue(json, Map.class);
            Object itemsObj = root.get("items");
            if (!(itemsObj instanceof List)) {
                System.out.println("[callBizNoAndParse] items 필드가 리스트가 아닙니다");
                return result;
            }

            List<?> lst = (List<?>) itemsObj;
            int idx = 0;
            for (Object o : lst) {
                idx++;
                if (!(o instanceof Map)) {
                    // null 이나 이상한 값 섞여 있을 수 있음
                    continue;
                }
                Map<?, ?> m = (Map<?, ?>) o;

                BizNoCandidate c = new BizNoCandidate();
                c.company = nvl((String) m.get("company"));
                c.bno = nvl((String) m.get("bno"));
                c.cno = nvl((String) m.get("cno"));

                String status = nvl((String) m.get("bstt"));
                String taxType = nvl((String) m.get("taxtype"));

                result.add(c);
            }

            System.out.println();
            System.out.println("=== BizNo API 검색 결과 ===");
            System.out.println("검색어: " + companyName);
            System.out.println("총 후보 수: " + result.size());
            System.out.println("---------------------------------------------");
            int no = 1;
            for (BizNoCandidate c : result) {
                System.out.printf("%d) 회사명: %s | BNO: %s | CNO: %s%n",
                        no++, c.company, c.bno, c.cno);
            }
            System.out.println("---------------------------------------------");
            System.out.println();

        } catch (Exception e) {
            System.out.println("[callBizNoAndParse] 예외 발생: " + e.getMessage());
        }

        return result;
    }

    // =================== 금융위 기업개요조회 ===================

    private String callFssByCrno(String crnoDigits) {
        if (isEmpty(crnoDigits))
            return null;

        if (fssCorpInfoUrl == null || fssCorpInfoUrl.trim().isEmpty()
                || fssApiKey == null || fssApiKey.trim().isEmpty()) {
            System.out.println("[callFssByCrno] fss.api-url 또는 fss.service-key 설정 없음. FSS API 호출 스킵");
            return null;
        }

        try {
            String query = "crno=" + URLEncoder.encode(crnoDigits, "UTF-8")
                    + "&serviceKey=" + URLEncoder.encode(fssApiKey, "UTF-8");

            String urlStr = fssCorpInfoUrl + "?" + query;
            String xml = httpGet(urlStr);
            System.out.println("[금융위 기업개요 조회] : " + xml);
            return xml;
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
        // 1순위: cno(법인등록번호)가 있는 것
        List<BizNoCandidate> hasCorpNo = new ArrayList<>();
        for (BizNoCandidate c : candidates) {
            if (!isEmpty(c.cno)) {
                hasCorpNo.add(c);
            }
        }
        if (!hasCorpNo.isEmpty()) {
            System.out.println("[filterCandidates] 1순위(법인번호 존재) 후보 수=" + hasCorpNo.size());
            return hasCorpNo;
        }

        // 2순위: 상호에 "본사", "중앙", "(주)" 등 본사 느낌 나는 후보 필터링 하려면 여기서 추가 로직도 가능
        // 지금은 예시로, 일단 그대로 사용
        System.out.println("[filterCandidates] 3순위(모든 후보 사용) 후보 수=" + candidates.size());
        return candidates;
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

        List<MatchedCompany> scoredList = new ArrayList<>();

        for (BizNoCandidate c : candidates) {
            String baseName = !isEmpty(c.fssCorpName) ? c.fssCorpName : c.company;
            String normCandName = normalizeCompanyName(baseName);

            int score = calcNameSimilarityScore(normCardName, normCandName);

            // 주소 점수
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
                System.out.println();
                System.out.println("=== 회사 매칭 결과 (1st Candidate) ===");
                printCandidateDetail(best);
                System.out.println();
                System.out.println("=== 회사 매칭 결과 (2nd Candidate) ===");
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

        if (company.getName() == null || company.getName().isEmpty()) {
            company.setName(coalesce(c.fssCorpName, c.company));
        }

        if (company.getRepName() == null || company.getRepName().isEmpty()) {
            company.setRepName(nvl(c.fssRepName));
        }

        if (!isEmpty(bizNoDigits)) {
            company.setBizNo(bizNoDigits);
        }

        if (!isEmpty(corpNoDigits)) {
            company.setCorpNo(corpNoDigits);
        }

        if (company.getAddress() == null || company.getAddress().isEmpty()) {
            company.setAddress(nvl(c.fssAddress));
        }

        if (company.getHomepage() == null || company.getHomepage().isEmpty()) {
            company.setHomepage(nvl(c.fssHomepage));
        }

        if (company.getCreatedAt() == null) {
            company.setCreatedAt(LocalDateTime.now());
        }
        company.setUpdatedAt(LocalDateTime.now());
    }

    /**
     * BizNo/FSS 매칭 실패 시, corpCode.csv(DART)만 가지고
     * 회사명을 유사도로 매칭해서 Company를 생성/업데이트하는 fallback 로직
     */
    private Optional<Company> matchFromDartAndSave(String companyName, String address) {

        if (dartCorpCodes == null || dartCorpCodes.isEmpty()) {
            System.out.println("[matchFromDartAndSave] dartCorpCodes 비어 있음 → DART 매칭 불가");
            return Optional.empty();
        }

        if (isEmpty(companyName)) {
            System.out.println("[matchFromDartAndSave] companyName이 비어 있음 → 매칭 불가");
            return Optional.empty();
        }

        String normName = normalizeCompanyName(companyName);

        DartCorpCode best = null;
        DartCorpCode second = null;
        int bestScore = 0;
        int secondScore = 0;

        for (DartCorpCode c : dartCorpCodes) {
            if (isEmpty(c.corpName))
                continue;

            String normCorpName = normalizeCompanyName(c.corpName);
            int score = calcNameSimilarityScore(normName, normCorpName);
            if (score <= 0)
                continue;

            if (score > bestScore) {
                second = best;
                secondScore = bestScore;
                best = c;
                bestScore = score;
            } else if (score > secondScore) {
                second = c;
                secondScore = score;
            }
        }

        if (best == null) {
            System.out.println("[matchFromDartAndSave] 스코어가 0보다 큰 후보가 없음 → 매칭 포기");
            return Optional.empty();
        }

        // 최소 점수 기준(너무 애매한 매칭 방지)
        if (bestScore < 60) {
            System.out.println("[matchFromDartAndSave] bestScore=" + bestScore + " (60 미만) → 매칭 포기");
            return Optional.empty();
        }

        // 상위 2개가 너무 비슷하면 포기 (BizNo 쪽 로직과 동일한 느낌으로)
        if (second != null && (bestScore - secondScore) < 10) {
            System.out.println("[matchFromDartAndSave] DART 상위 2개 점수 차이 작음 → 매칭 포기");
            System.out
                    .println(" 1st: " + best.corpName + " (score=" + bestScore + ", stockCode=" + best.stockCode + ")");
            System.out.println(
                    " 2nd: " + second.corpName + " (score=" + secondScore + ", stockCode=" + second.stockCode + ")");
            return Optional.empty();
        }

        System.out.println("[matchFromDartAndSave] 최종 DART 매칭 성공, score=" + bestScore);
        System.out.println(
                " corpName=" + best.corpName + ", stockCode=" + best.stockCode + ", corpCode=" + best.corpCode);

        // 1) 이름으로 기존 회사 찾기
        Optional<Company> existedOpt = companyRepository.findByName(best.corpName);
        Company company = existedOpt.orElseGet(Company::new);

        // 2) 필드 세팅 (BizNo/법인번호는 DART CSV에 없으니 이름/주소 위주)
        if (isEmpty(company.getName())) {
            company.setName(best.corpName);
        }

        if (!isEmpty(address) && (company.getAddress() == null || company.getAddress().isEmpty())) {
            company.setAddress(address);
        }

        if (company.getCreatedAt() == null) {
            company.setCreatedAt(LocalDateTime.now());
        }
        company.setUpdatedAt(LocalDateTime.now());

        Company saved = companyRepository.save(company);
        return Optional.of(saved);
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
        System.out.println("최종 점수: " + mc.score);
        System.out.println("---------------------------------------------");
        System.out.println("[BIZNO 정보]");
        System.out.println("- 회사명      : " + c.company);
        System.out.println("- 사업자번호  : " + coalesce(c.fssBizNo, normalizeNumber(c.bno)));
        System.out.println("- 법인번호    : " + normalizeNumber(c.cno));
        System.out.println();
        System.out.println("[FSS 정보(보강)]");
        System.out.println("- 기업명      : " + c.fssCorpName);
        System.out.println("- 대표자명    : " + c.fssRepName);
        System.out.println("- 주소        : " + c.fssAddress);
        System.out.println("- 홈페이지    : " + c.fssHomepage);
        System.out.println("---------------------------------------------");
    }
}
