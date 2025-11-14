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
import java.util.*;

@Service
public class CompanyInfoExtractServiceImpl implements CompanyInfoExtractService {

    @Value("${company-lookup.bizno.url:https://bizno.net/api/fapi}")
    private String bizNoApiUrl;

    @Value("${company-lookup.bizno.key:HTbSHc9nGsisBxBdGvuOH0pn2v9m}")
    private String bizNoApiKey;

    @Value("${company-lookup.fss.url:https://apis.data.go.kr/1160100/service/GetCorpBasicInfoService_V2/getCorpOutline_V2}")
    private String fssApiUrl;

    @Value("${company-lookup.fss.service-key:15ac2cb956d01239046f76ea9f2fd95296d3edf3e7a6c85de47208558a52b800}")
    private String fssServiceKey;

    private final CompanyRepository companyRepository;
    private final ObjectMapper objectMapper;

    public CompanyInfoExtractServiceImpl(CompanyRepository companyRepository,
            ObjectMapper objectMapper) {
        this.companyRepository = companyRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<Company> extractAndSave(String companyName, String address) {
        if (isEmpty(companyName))
            return Optional.empty();

        List<BizNoCandidate> candidates = callBizNoAndParse(companyName);
        if (candidates.isEmpty())
            return Optional.empty();

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

        List<BizNoCandidate> filtered = new ArrayList<BizNoCandidate>();
        for (BizNoCandidate c : candidates) {
            if (isEmpty(c.cno))
                continue;
            if (isEmpty(c.fssAddress))
                continue;
            filtered.add(c);
        }
        if (filtered.isEmpty())
            return Optional.empty();

        Optional<MatchedCompany> matchedOpt = matchCompany(companyName, address, filtered);
        if (!matchedOpt.isPresent())
            return Optional.empty();

        MatchedCompany matched = matchedOpt.get();

        String bizNoDigits = normalizeNumber(coalesce(matched.candidate.fssBizNo, matched.candidate.bno));
        String corpNoDigits = normalizeNumber(matched.candidate.cno);

        Optional<Company> byBizNo = isEmpty(bizNoDigits) ? Optional.<Company>empty()
                : companyRepository.findByBizNo(bizNoDigits);
        Optional<Company> byCorpNo = isEmpty(corpNoDigits) ? Optional.<Company>empty()
                : companyRepository.findByCorpNo(corpNoDigits);

        Company company = byBizNo.orElseGet(() -> byCorpNo.orElseGet(Company::new));

        fillCompanyFromMatched(company, matched, bizNoDigits, corpNoDigits);

        Company saved = companyRepository.save(company);
        return Optional.of(saved);
    }

    // =================== BizNo 호출 ===================

    private List<BizNoCandidate> callBizNoAndParse(String companyName) {
        List<BizNoCandidate> result = new ArrayList<BizNoCandidate>();
        BufferedReader rd = null;
        HttpURLConnection conn = null;
        try {
            StringBuilder urlBuilder = new StringBuilder(bizNoApiUrl);
            urlBuilder.append("?key=").append(URLEncoder.encode(bizNoApiKey, "UTF-8"));
            urlBuilder.append("&gb=").append(URLEncoder.encode("3", "UTF-8"));
            urlBuilder.append("&q=").append(URLEncoder.encode(companyName, "UTF-8"));
            urlBuilder.append("&type=").append(URLEncoder.encode("xml", "UTF-8"));
            urlBuilder.append("&pagecnt=").append(URLEncoder.encode("20", "UTF-8"));

            URL url = new URL(urlBuilder.toString());
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-type", "application/xml");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(5000);

            int code = conn.getResponseCode();
            if (code >= 200 && code <= 300) {
                rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            } else {
                rd = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
            }

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line).append("\n");
            }

            result = parseBizNoXml(sb.toString());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (rd != null)
                    rd.close();
            } catch (IOException ignore) {
            }
            if (conn != null)
                conn.disconnect();
        }
        return result;
    }

    private List<BizNoCandidate> parseBizNoXml(String xml) throws Exception {
        List<BizNoCandidate> list = new ArrayList<BizNoCandidate>();
        if (xml == null || xml.trim().isEmpty())
            return list;

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xml)));

        NodeList itemNodes = doc.getElementsByTagName("item");
        for (int i = 0; i < itemNodes.getLength(); i++) {
            Element e = (Element) itemNodes.item(i);
            String company = getTagText(e, "company");
            String bno = getTagText(e, "bno");
            String cno = getTagText(e, "cno");
            list.add(new BizNoCandidate(company, bno, cno));
        }
        return list;
    }

    // =================== FSS 호출 ===================

    private String callFssByCrno(String crno) {
        BufferedReader rd = null;
        HttpURLConnection conn = null;
        try {
            StringBuilder urlBuilder = new StringBuilder(fssApiUrl);
            urlBuilder.append("?serviceKey=").append(URLEncoder.encode(fssServiceKey, "UTF-8"));
            urlBuilder.append("&pageNo=1");
            urlBuilder.append("&numOfRows=10");
            urlBuilder.append("&resultType=xml");
            urlBuilder.append("&crno=").append(URLEncoder.encode(crno, "UTF-8"));

            URL url = new URL(urlBuilder.toString());
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-type", "application/xml");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(5000);

            int code = conn.getResponseCode();
            if (code < 200 || code > 300) {
                rd = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
                StringBuilder err = new StringBuilder();
                String line;
                while ((line = rd.readLine()) != null) {
                    err.append(line).append("\n");
                }
                System.out.println("[FSS ERROR] " + err);
                return "";
            }

            rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = rd.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        } finally {
            try {
                if (rd != null)
                    rd.close();
            } catch (IOException ignore) {
            }
            if (conn != null)
                conn.disconnect();
        }
    }

    private FssCorpInfo parseFssInfo(String xml) {
        if (xml == null || xml.trim().isEmpty())
            return null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xml)));

            NodeList itemNodes = doc.getElementsByTagName("item");
            if (itemNodes.getLength() == 0)
                return null;

            Element e = (Element) itemNodes.item(0);

            FssCorpInfo info = new FssCorpInfo();
            info.corpNm = getTagText(e, "corpNm");
            info.enpRprFnm = getTagText(e, "enpRprFnm");
            info.bzno = getTagText(e, "bzno");
            info.enpBsadr = getTagText(e, "enpBsadr");
            info.enpHmpgUrl = getTagText(e, "enpHmpgUrl");
            return info;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // =================== 매칭 로직 (이전 것 그대로) ===================

    // ... (여기에는 앞에서 이미 사용하던 matchCompany / calcNameSimilarityScore /
    // calcAddressSimilarityScore / breakTie / printCandidateDetail 등
    // 그대로 두면 됨. 변경사항 없음이라, 길이 때문에 생략해도 됨)
    //
    // 위에서 너가 붙여놓았던 버전 그대로 쓰면 된다.
    // 핵심은 extractAndSave( )는 안 건드렸고, HTTP 타임아웃만 추가했다는 점.

    // =================== 내부 DTO / 유틸 ===================

    private static class BizNoCandidate {
        String company;
        String bno;
        String cno;
        String fssCorpName;
        String fssRepName;
        String fssBizNo;
        String fssAddress;
        String fssHomepage;

        BizNoCandidate(String company, String bno, String cno) {
            this.company = company;
            this.bno = bno;
            this.cno = cno;
        }
    }

    private static class MatchedCompany {
        BizNoCandidate candidate;
        int score;

        MatchedCompany(BizNoCandidate candidate, int score) {
            this.candidate = candidate;
            this.score = score;
        }
    }

    private static class FssCorpInfo {
        String corpNm;
        String enpRprFnm;
        String bzno;
        String enpBsadr;
        String enpHmpgUrl;
    }

    private static String getTagText(Element parent, String tagName) {
        NodeList list = parent.getElementsByTagName(tagName);
        if (list.getLength() == 0)
            return "";
        Node n = list.item(0);
        if (n == null)
            return "";
        Node c = n.getFirstChild();
        return c == null ? "" : c.getNodeValue();
    }

    private static String normalizeNumber(String num) {
        if (num == null)
            return null;
        return num.replaceAll("[^0-9]", "");
    }

    private static String normalizeCompanyName(String raw) {
        if (raw == null)
            return "";
        String s = raw.trim();
        s = s.replace("㈜", "");
        s = s.replace("(주)", "");
        s = s.replace("주식회사", "");
        s = s.replaceAll("[()\\[\\]]", " ");
        s = s.replaceAll("\\s+", " ");
        return s;
    }

    private static String normalizeAddress(String raw) {
        if (raw == null)
            return "";
        String s = raw.trim();
        s = s.replaceAll("\\s+", " ");
        return s;
    }

    private static String extractRegionKey(String address) {
        if (address == null || address.isEmpty())
            return "";
        String[] tokens = address.split(" ");
        if (tokens.length < 2)
            return address;
        return tokens[0] + " " + tokens[1];
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }

    private static boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String coalesce(String a, String b) {
        if (!isEmpty(a))
            return a;
        return b;
    }

    // 이름 유사도 점수 (0~100)
    private int calcNameSimilarityScore(String a, String b) {
        if (isEmpty(a) || isEmpty(b))
            return 0;
        if (a.equals(b))
            return 100;

        String[] at = a.split(" ");
        String[] bt = b.split(" ");

        Set<String> setA = new HashSet<String>();
        for (String x : at) {
            x = x.trim();
            if (!x.isEmpty())
                setA.add(x);
        }
        Set<String> setB = new HashSet<String>();
        for (String y : bt) {
            y = y.trim();
            if (!y.isEmpty())
                setB.add(y);
        }

        int common = 0;
        for (String x : setA) {
            if (setB.contains(x))
                common++;
        }

        int score;

        if (common > 0) {
            double ratio = (double) common / Math.max(setA.size(), setB.size());
            score = 50 + (int) Math.round(ratio * 30);
            String firstA = at.length > 0 ? at[0].trim() : "";
            String firstB = bt.length > 0 ? bt[0].trim() : "";
            if (!firstA.isEmpty() && firstA.equals(firstB)) {
                score += 10;
            }
        } else {
            if (a.contains(b) || b.contains(a)) {
                score = 60;
            } else {
                score = 0;
            }
        }

        int lenA = a.length();
        int lenB = b.length();
        double lenRatio = (double) Math.min(lenA, lenB) / Math.max(lenA, lenB);
        if (lenRatio < 0.5) {
            score -= 10;
        }

        if (score < 0)
            score = 0;
        if (score > 95)
            score = 95;
        return score;
    }

    private int calcAddressSimilarityScore(String cardAddr, String candAddr) {
        if (isEmpty(cardAddr) || isEmpty(candAddr))
            return 0;

        String cardRegion = extractRegionKey(cardAddr);
        String candRegion = extractRegionKey(candAddr);

        if (!isEmpty(cardRegion) && cardRegion.equals(candRegion)) {
            return 20;
        }

        String normCard = normalizeAddress(cardAddr);
        String normCand = normalizeAddress(candAddr);

        if (normCard.contains(candAddr) || normCand.contains(cardAddr)) {
            return 10;
        }
        return 0;
    }

    private MatchedCompany breakTie(MatchedCompany a, MatchedCompany b) {
        BizNoCandidate ca = a.candidate;
        BizNoCandidate cb = b.candidate;

        String caCno = normalizeNumber(ca.cno);
        String cbCno = normalizeNumber(cb.cno);
        int lenCna = caCno == null ? 0 : caCno.length();
        int lenCnb = cbCno == null ? 0 : cbCno.length();
        if (lenCna == 13 && lenCnb != 13)
            return a;
        if (lenCnb == 13 && lenCna != 13)
            return b;

        String caBno = normalizeNumber(ca.bno);
        String cbBno = normalizeNumber(cb.bno);
        int lenBna = caBno == null ? 0 : caBno.length();
        int lenBnb = cbBno == null ? 0 : cbBno.length();
        if (lenBna == 10 && lenBnb != 10)
            return a;
        if (lenBnb == 10 && lenBna != 10)
            return b;

        if (!isEmpty(ca.fssAddress) && isEmpty(cb.fssAddress))
            return a;
        if (!isEmpty(cb.fssAddress) && isEmpty(ca.fssAddress))
            return b;

        return a;
    }

    private void fillCompanyFromMatched(Company company,
            MatchedCompany matched,
            String bizNoDigits,
            String corpNoDigits) {
        BizNoCandidate c = matched.candidate;

        company.setName(coalesce(c.fssCorpName, c.company));
        company.setRepName(nvl(c.fssRepName));
        company.setBizNo(bizNoDigits);
        company.setCorpNo(corpNoDigits);
        company.setAddress(nvl(c.fssAddress));
        company.setHomepage(nvl(c.fssHomepage));
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

        List<BizNoCandidate> exactMatches = new ArrayList<BizNoCandidate>();
        for (BizNoCandidate c : candidates) {
            if (isEmpty(c.fssAddress))
                continue;

            String normCandName = normalizeCompanyName(
                    isEmpty(c.fssCorpName) ? c.company : c.fssCorpName);
            String normCandAddr = normalizeAddress(c.fssAddress);

            if (normCardName.equals(normCandName) && normCardAddr.equals(normCandAddr)) {
                exactMatches.add(c);
            }
        }

        if (!exactMatches.isEmpty()) {
            BizNoCandidate best = exactMatches.get(0);
            System.out.println("[매칭 성공] 회사명 + 주소 완전 동일 후보 발견 (1순위 확정)");
            MatchedCompany mc = new MatchedCompany(best, 100);
            printCandidateDetail(mc);
            return Optional.of(mc);
        }

        List<MatchedCompany> scoredList = new ArrayList<MatchedCompany>();

        for (BizNoCandidate c : candidates) {
            if (isEmpty(c.fssCorpName) && isEmpty(c.company))
                continue;

            String normCandName = normalizeCompanyName(
                    isEmpty(c.fssCorpName) ? c.company : c.fssCorpName);

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

        Collections.sort(scoredList, (a, b) -> {
            int cmp = Integer.compare(b.score, a.score);
            if (cmp != 0)
                return cmp;
            MatchedCompany chosen = breakTie(a, b);
            return (chosen == a) ? -1 : 1;
        });

        MatchedCompany first = scoredList.get(0);

        System.out.println("[매칭 성공] 이름+주소 점수 기반 최상위 후보 선택");
        if (scoredList.size() > 1) {
            MatchedCompany second = scoredList.get(1);
            System.out.println(" 1위 score=" + first.score
                    + " / 2위 score=" + second.score);
        }
        printCandidateDetail(first);

        return Optional.of(first);
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
