package com.bbey.neez.service;

import com.bbey.neez.repository.BizCardRepository;
import com.bbey.neez.entity.BizCard;
import com.bbey.neez.entity.Company;
import com.bbey.neez.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class BizCardReaderServiceImpl implements BizCardReaderService {

    @Value("${ocr.apigw.url:https://example-api-gateway.local/ocr}")
    private String APIGW_URL;

    @Value("${ocr.apigw.secret:REPLACE_WITH_REAL_SECRET}")
    private String SECRET;

    @Autowired
    private BizCardRepository bizCardRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private com.bbey.neez.repository.UserRepository userRepository;

    /**
     * 컨트롤러에서 파일명만 넘기면
     * 1) OCR 호출하고
     * 2) 명함 필드만 뽑아서
     * 3) JSON으로 내려줄 수 있게 Map으로 돌려준다.
     */
    @Override
    public Map<String, String> readBizCard(String fileName) {
        Map<String, String> result = new LinkedHashMap<>();
        try {
            String payload = buildPayloadWithFile("src/main/resources/BizCard/" + fileName);

            // placeholder면 모의 응답
            if (APIGW_URL != null && APIGW_URL.contains("example-api-gateway")) {
                String mock = "{\"mock\":\"ok\"}";
                result.put("success", "true");
                result.put("raw", mock);
                result.put("data", null);
                return result;
            }

            HttpURLConnection conn = (HttpURLConnection) new URL(APIGW_URL).openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            conn.setRequestProperty("X-OCR-SECRET", SECRET);
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.getBytes("UTF-8"));
            }

            int code = conn.getResponseCode();
            InputStream is = (code == 200) ? conn.getInputStream() : conn.getErrorStream();
            String resp = readAll(is);

            // 원본 저장(선택)
            try (FileOutputStream fos = new FileOutputStream("result.json")) {
                fos.write(resp.getBytes("UTF-8"));
            }

            // 여기서 우리가 쓰는 명함 필드만 추출
            Map<String, String> cardData = parseNameCardFromJson(resp);

            // result.put("success", code == 200);
            // result.put("raw", resp);          // 필요하면 원본도 내려보내
            // result.put("data", cardData);     // 프론트에서 쓸 핵심값
            return parseNameCardFromJson(resp);

        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    /**
     * OCR JSON에서 필요한 값만 뽑아오는 부분
     */
    private Map<String, String> parseNameCardFromJson(String json) {
        String name       = extractFirstText(json, "\"name\"\\s*:\\s*\\[\\s*\\{[\\s\\S]*?\"text\"\\s*:\\s*\"(.*?)\"");
        String department = extractFirstText(json, "\"department\"\\s*:\\s*\\[\\s*\\{[\\s\\S]*?\"text\"\\s*:\\s*\"(.*?)\"");
        String position   = extractFirstText(json, "\"position\"\\s*:\\s*\\[\\s*\\{[\\s\\S]*?\"text\"\\s*:\\s*\"(.*?)\"");
        String mobile     = extractFirstText(json, "\"mobile\"\\s*:\\s*\\[\\s*\\{[\\s\\S]*?\"text\"\\s*:\\s*\"(.*?)\"");
        String tel        = extractFirstText(json, "\"tel\"\\s*:\\s*\\[\\s*\\{[\\s\\S]*?\"text\"\\s*:\\s*\"(.*?)\"");
        String email      = extractFirstText(json, "\"email\"\\s*:\\s*\\[\\s*\\{[\\s\\S]*?\"text\"\\s*:\\s*\"(.*?)\"");
        String address    = extractFirstText(json, "\"address\"\\s*:\\s*\\[\\s*\\{[\\s\\S]*?\"text\"\\s*:\\s*\"(.*?)\"");

        String fax = firstNonNull(
                extractFirstText(json, "\"fax\"\\s*:\\s*\\[\\s*\\{[\\s\\S]*?\"text\"\\s*:\\s*\"(.*?)\""),
                extractFirstText(json, "\"faxNo\"\\s*:\\s*\\[\\s*\\{[\\s\\S]*?\"text\"\\s*:\\s*\"(.*?)\"")
        );

        String company = firstNonNull(
                extractFirstText(json, "\"company\"\\s*:\\s*\\[\\s*\\{[\\s\\S]*?\"text\"\\s*:\\s*\"(.*?)\""),
                extractFirstText(json, "\"companyName\"\\s*:\\s*\\[\\s\\S]*?\"text\"\\s*:\\s*\"(.*?)\""),
                extractFirstText(json, "\"organization\"\\s*:\\s*\\[\\s*\\{[\\s\\S]*?\"text\"\\s*:\\s*\"(.*?)\""),
                extractFirstText(json, "\"org\"\\s*:\\s*\\[\\s*\\{[\\s\\S]*?\"text\"\\s*:\\s*\"(.*?)\""),
                extractFirstText(json, "\"corp\"\\s*:\\s*\\[\\s*\\{[\\s\\S]*?\"text\"\\s*:\\s*\"(.*?)\""),
                extractFirstText(json, "\"office\"\\s*:\\s*\\[\\s*\\{[\\s\\S]*?\"text\"\\s*:\\s*\"(.*?)\"")
        );

        Map<String, String> map = new LinkedHashMap<>();
        map.put("company",   val(company));
        map.put("name",      val(name));
        map.put("department",val(department));
        map.put("position",  val(position));
        map.put("tel",       val(tel));
        map.put("mobile",    val(mobile));
        map.put("fax",       val(fax));
        map.put("email",     val(email));
        map.put("address",   val(address));

        return map;
    }

    /**
     * OCR로 뽑은 data를 실제 DB에 저장하는 메서드
     */
    @Override
    public BizCard saveBizCardFromOcr(Map<String, String> data, Long user_idx) {
        // 1. 회사부터 처리
        String companyName = nvl(data.get("company"));
        Long companyIdx = null;
        if (!companyName.isEmpty()) {
            companyIdx = companyRepository
                    .findByName(companyName)
                    .map(Company::getIdx)
                    .orElseGet(() -> {
                        Company c = new Company();
                        c.setName(companyName);
                        c.setCreated_at(java.time.LocalDateTime.now());
                        c.setUpdated_at(java.time.LocalDateTime.now());
                        return companyRepository.save(c).getIdx();
                    });
        }

        // 2. 명함 저장
        BizCard card = new BizCard();
        // user_idx가 null 또는 <=0이거나 존재하지 않으면, 자동으로 더미 Users 레코드를 만들어 그 id를 사용합니다.
        Long finalUserId = null;
        if (user_idx != null && user_idx > 0L) {
            if (userRepository.existsById(user_idx)) {
                finalUserId = user_idx;
            } else {
                System.out.println("Warning: user_idx=" + user_idx + " not found in users table. Will create placeholder user.");
            }
        }

        if (finalUserId == null) {
            // 생성 시 필요한 최소 필드만 넣습니다. 실제 비즈니스에서는 별도 정책 필요.
            com.bbey.neez.entity.Users u = new com.bbey.neez.entity.Users();
            u.setName("auto_generated");
            u.setCreated_at(java.time.LocalDateTime.now());
            u.setUpdated_at(java.time.LocalDateTime.now());
            com.bbey.neez.entity.Users savedUser = userRepository.save(u);
            finalUserId = savedUser.getIdx();
            System.out.println("Info: created placeholder user with id=" + finalUserId);
        }

        // entity의 user_idx는 String 타입이므로 문자열로 저장
        card.setUser_idx(finalUserId);
        card.setName(nvl(data.get("name")));
        card.setCompany_idx(companyIdx != null ? companyIdx : 0L);
        card.setDepartment(nvl(data.get("department")));
        card.setPosition(nvl(data.get("position")));
        card.setEmail(nvl(data.get("email")));
        card.setPhone_number(nvl(data.get("mobile")));     // 휴대폰
        card.setLine_number(nvl(data.get("tel")));         // 회사번호
        card.setFax_number(nvl(data.get("fax")));
        card.setAddress(nvl(data.get("address")));
        card.setMemo("");                                  // OCR에서는 없으니까 빈값
        card.setCreated_at(java.time.LocalDateTime.now());
        card.setUpdated_at(java.time.LocalDateTime.now());

        return bizCardRepository.save(card);
    }

    // ── 이하 유틸은 네 기존 코드 그대로 ─────────────────────────────

    private static String buildPayloadWithFile(String filePath) throws IOException {
        File f = new File(filePath);
        if (!f.exists() || !f.isFile()) {
            throw new FileNotFoundException("이미지 파일을 찾을 수 없습니다: " + f.getAbsolutePath());
        }
        byte[] img = Files.readAllBytes(f.toPath());
        String b64 = Base64.getEncoder().encodeToString(img);
        String ext = getExt(filePath);
        return "{"
                + "\"version\":\"V2\","
                + "\"requestId\":\"" + UUID.randomUUID() + "\","
                + "\"timestamp\":" + System.currentTimeMillis() + ","
                + "\"lang\":\"ko\","
                + "\"images\":[{"
                +   "\"format\":\"" + (ext.isEmpty() ? "jpg" : ext) + "\","
                +   "\"name\":\"bizcard\","
                +   "\"data\":\"" + b64 + "\""
                + "}]"
                + "}";
    }

    private static String readAll(InputStream is) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            StringBuilder sb = new StringBuilder();
            for (String line; (line = br.readLine()) != null;) {
                sb.append(line).append('\n');
            }
            return sb.toString();
        }
    }

    private static String getExt(String path) {
        int i = path.lastIndexOf('.');
        return (i >= 0) ? path.substring(i + 1).toLowerCase() : "";
    }

    private static String extractFirstText(String json, String regex) {
        Pattern p = Pattern.compile(regex, Pattern.DOTALL | Pattern.UNICODE_CASE);
        Matcher m = p.matcher(json);
        return m.find() ? unescape(m.group(1)) : null;
    }

    private static String unescape(String s) {
        if (s == null || s.indexOf('\\') < 0) return s;
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char n = s.charAt(i + 1);
                if (n == 'u' && i + 5 < s.length()) {
                    String hex = s.substring(i + 2, i + 6);
                    try {
                        out.append((char) Integer.parseInt(hex, 16));
                        i += 5;
                        continue;
                    } catch (NumberFormatException ignore) {}
                }
                if (n == '"' || n == '\\' || n == '/') { out.append(n); i++; continue; }
                if (n == 'b') { out.append('\b'); i++; continue; }
                if (n == 'f') { out.append('\f'); i++; continue; }
                if (n == 'n') { out.append('\n'); i++; continue; }
                if (n == 'r') { out.append('\r'); i++; continue; }
                if (n == 't') { out.append('\t'); i++; continue; }
            }
            out.append(c);
        }
        return out.toString();
    }

    private static String firstNonNull(String... vals) {
        for (String v : vals) if (v != null && !v.isEmpty()) return v;
        return null;
    }

    private static String val(String s) {
        return (s == null || s.isEmpty()) ? "" : s;
    }

    private String nvl(String s) {
        return (s == null) ? "" : s;
    }
}
