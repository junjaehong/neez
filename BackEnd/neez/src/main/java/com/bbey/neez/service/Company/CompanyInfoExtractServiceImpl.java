package com.bbey.neez.service.Company;

import com.bbey.neez.entity.Company;
import com.bbey.neez.repository.CompanyRepository;
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

    public CompanyInfoExtractServiceImpl(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public Optional<Company> extractAndSave(String companyName, String address) {
        if (isEmpty(companyName)) return Optional.empty();

        // 1) BizNo 후보
        List<BizNoCandidate> candidates = callBizNoAndParse(companyName);
        if (candidates.isEmpty()) return Optional.empty();

        // 2) FSS 정보 채우기
        for (BizNoCandidate c : candidates) {
            if (isEmpty(c.cno)) continue;
            String crnoDigits = normalizeNumber(c.cno);
            String fssXml = callFssByCrno(crnoDigits);
            FssCorpInfo info = parseFssInfo(fssXml);
            if (info != null) {
                c.fssCorpName  = nvl(info.corpNm);
                c.fssRepName   = nvl(info.enpRprFnm);
                c.fssBizNo     = normalizeNumber(info.bzno);
                c.fssAddress   = nvl(info.enpBsadr);
                c.fssHomepage  = nvl(info.enpHmpgUrl);
            }
        }

        // 2-1) 법인등록번호 + 주소 있는 애들만
        List<BizNoCandidate> filtered = new ArrayList<>();
        for (BizNoCandidate c : candidates) {
            if (isEmpty(c.cno)) continue;
            if (isEmpty(c.fssAddress)) continue;
            filtered.add(c);
        }
        if (filtered.isEmpty()) return Optional.empty();

        // 3) 매칭
        Optional<MatchedCompany> matchedOpt = matchCompany(companyName, address, filtered);
        if (!matchedOpt.isPresent()) return Optional.empty();

        MatchedCompany matched = matchedOpt.get();

        // 4) biz_no / corp_no로 기존 회사 찾기
        String bizNoDigits  = normalizeNumber(coalesce(matched.candidate.fssBizNo, matched.candidate.bno));
        String corpNoDigits = normalizeNumber(matched.candidate.cno);

        Optional<Company> byBizNo  = isEmpty(bizNoDigits)  ? Optional.empty() : companyRepository.findByBizNo(bizNoDigits);
        Optional<Company> byCorpNo = isEmpty(corpNoDigits) ? Optional.empty() : companyRepository.findByCorpNo(corpNoDigits);

        Company company = byBizNo.orElseGet(() -> byCorpNo.orElseGet(Company::new));

        // 5) 값 채우기
        fillCompanyFromMatched(company, matched, bizNoDigits, corpNoDigits);

        // 6) 저장
        Company saved = companyRepository.save(company);
        return Optional.of(saved);
    }

    // =================== BizNo 호출 ===================

    private List<BizNoCandidate> callBizNoAndParse(String companyName) {
        List<BizNoCandidate> result = new ArrayList<>();
        BufferedReader rd = null;
        HttpURLConnection conn = null;
        try {
            StringBuilder urlBuilder = new StringBuilder(bizNoApiUrl);
            urlBuilder.append("?key=").append(URLEncoder.encode(bizNoApiKey, "UTF-8"));
            urlBuilder.append("&gb=").append(URLEncoder.encode("3", "UTF-8")); // 3: 상호명 검색
            urlBuilder.append("&q=").append(URLEncoder.encode(companyName, "UTF-8"));
            urlBuilder.append("&type=").append(URLEncoder.encode("xml", "UTF-8"));
            urlBuilder.append("&pagecnt=").append(URLEncoder.encode("20", "UTF-8"));

            URL url = new URL(urlBuilder.toString());
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-type", "application/xml");

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
            try { if (rd != null) rd.close(); } catch (IOException ignore) {}
            if (conn != null) conn.disconnect();
        }
        return result;
    }

    private List<BizNoCandidate> parseBizNoXml(String xml) throws Exception {
        List<BizNoCandidate> list = new ArrayList<>();
        if (xml == null || xml.trim().isEmpty()) return list;

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
            try { if (rd != null) rd.close(); } catch (IOException ignore) {}
            if (conn != null) conn.disconnect();
        }
    }

    private FssCorpInfo parseFssInfo(String xml) {
        if (xml == null || xml.trim().isEmpty()) return null;
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xml)));

            NodeList itemNodes = doc.getElementsByTagName("item");
            if (itemNodes.getLength() == 0) return null;

            Element e = (Element) itemNodes.item(0);

            FssCorpInfo info = new FssCorpInfo();
            info.corpNm     = getTagText(e, "corpNm");
            info.enpRprFnm  = getTagText(e, "enpRprFnm");
            info.bzno       = getTagText(e, "bzno");
            info.enpBsadr   = getTagText(e, "enpBsadr");
            info.enpHmpgUrl = getTagText(e, "enpHmpgUrl");
            return info;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // =================== 매칭 로직 ===================

    private Optional<MatchedCompany> matchCompany(
            String cardCompanyName,
            String cardAddress,
            List<BizNoCandidate> candidates) {

        if (isEmpty(cardAddress)) return Optional.empty();

        String normCardName = normalizeCompanyName(cardCompanyName);
        String normCardAddr = normalizeAddress(cardAddress);
        String cardRegionKey = extractRegionKey(normCardAddr);

        List<MatchedCompany> matchedList = new ArrayList<>();

        for (BizNoCandidate c : candidates) {
            if (isEmpty(c.cno)) continue;
            if (isEmpty(c.fssAddress)) continue;

            String normCandName = normalizeCompanyName(
                    isEmpty(c.fssCorpName) ? c.company : c.fssCorpName
            );
            String normCandAddr = normalizeAddress(c.fssAddress);
            String candRegionKey = extractRegionKey(normCandAddr);

            int score = 0;

            if (normCardName.equals(normCandName)) {
                score += 50;
            } else if (normCandName.contains(normCardName) || normCardName.contains(normCandName)) {
                score += 20;
            }

            if (!cardRegionKey.isEmpty() && cardRegionKey.equals(candRegionKey)) {
                score += 40;
            }

            if (!normCardAddr.isEmpty()) {
                if (normCardAddr.equals(normCandAddr)) {
                    score += 60;
                } else if (normCandAddr.contains(normCardAddr)) {
                    score += 20;
                }
            }

            if (score >= 60) {
                matchedList.add(new MatchedCompany(c, score));
            }
        }

        if (matchedList.isEmpty()) return Optional.empty();

        matchedList.sort((o1, o2) -> Integer.compare(o2.score, o1.score));

        MatchedCompany first = matchedList.get(0);
        if (matchedList.size() == 1) return Optional.of(first);

        MatchedCompany second = matchedList.get(1);
        if (first.score - second.score >= 20) {
            return Optional.of(first);
        } else {
            return Optional.empty();
        }
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
        if (list.getLength() == 0) return "";
        Node n = list.item(0);
        if (n == null) return "";
        Node c = n.getFirstChild();
        return c == null ? "" : c.getNodeValue();
    }

    private static String normalizeNumber(String num) {
        if (num == null) return null;
        return num.replaceAll("[^0-9]", "");
    }

    private static String normalizeCompanyName(String raw) {
        if (raw == null) return "";
        String s = raw.trim();
        s = s.replace("㈜", "");
        s = s.replace("(주)", "");
        s = s.replace("주식회사", "");
        s = s.replaceAll("[()\\[\\]]", " ");
        s = s.replaceAll("\\s+", " ");
        return s;
    }

    private static String normalizeAddress(String raw) {
        if (raw == null) return "";
        String s = raw.trim();
        s = s.replaceAll("\\s+", " ");
        return s;
    }

    private static String extractRegionKey(String address) {
        if (address == null || address.isEmpty()) return "";
        String[] tokens = address.split(" ");
        if (tokens.length < 2) return address;
        return tokens[0] + " " + tokens[1];
    }

    private static String nvl(String s) {
        return s == null ? "" : s;
    }

    private static boolean isEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String coalesce(String a, String b) {
        if (!isEmpty(a)) return a;
        return b;
    }
}
