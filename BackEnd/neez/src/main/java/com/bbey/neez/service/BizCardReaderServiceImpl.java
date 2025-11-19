package com.bbey.neez.service;

import com.bbey.neez.repository.BizCardRepository;
import com.bbey.neez.entity.BizCard;
import com.bbey.neez.entity.Company;
import com.bbey.neez.entity.Users;
import com.bbey.neez.repository.CompanyRepository;
import com.bbey.neez.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDateTime;
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
    private UserRepository userRepository;

    @Override
    public Map<String, String> readBizCard(String fileName) {
        Map<String, String> result = new LinkedHashMap<>();
        try {
            String payload = buildPayloadWithFile("src/main/resources/BizCard/" + fileName);

            // Mock 처리
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

            // 원본 저장
            try (FileOutputStream fos = new FileOutputStream("result.json")) {
                fos.write(resp.getBytes("UTF-8"));
            }

            return parseNameCardFromJson(resp);

        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    private Map<String, String> parseNameCardFromJson(String json) {
        String name       = extractFirstText(json, "\"name\"[\\s\\S]*?\"text\"\\s*:\\s*\"(.*?)\"");
        String department = extractFirstText(json, "\"department\"[\\s\\S]*?\"text\"\\s*:\\s*\"(.*?)\"");
        String position   = extractFirstText(json, "\"position\"[\\s\\S]*?\"text\"\\s*:\\s*\"(.*?)\"");
        String mobile     = extractFirstText(json, "\"mobile\"[\\s\\S]*?\"text\"\\s*:\\s*\"(.*?)\"");
        String tel        = extractFirstText(json, "\"tel\"[\\s\\S]*?\"text\"\\s*:\\s*\"(.*?)\"");
        String email      = extractFirstText(json, "\"email\"[\\s\\S]*?\"text\"\\s*:\\s*\"(.*?)\"");
        String address    = extractFirstText(json, "\"address\"[\\s\\S]*?\"text\"\\s*:\\s*\"(.*?)\"");

        String fax = firstNonNull(
                extractFirstText(json, "\"fax\"[\\s\\S]*?\"text\"\\s*:\\s*\"(.*?)\""),
                extractFirstText(json, "\"faxNo\"[\\s\\S]*?\"text\"\\s*:\\s*\"(.*?)\"")
        );

        String company = firstNonNull(
                extractFirstText(json, "\"company\"[\\s\\S]*?\"text\"\\s*:\\s*\"(.*?)\""),
                extractFirstText(json, "\"organization\"[\\s\\S]*?\"text\"\\s*:\\s*\"(.*?)\""),
                extractFirstText(json, "\"corp\"[\\s\\S]*?\"text\"\\s*:\\s*\"(.*?)\"")
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

    @Override
    public BizCard saveBizCardFromOcr(Map<String, String> data, Long user_idx) {

        // 1) 회사 처리
        String companyName = nvl(data.get("company"));
        Long companyIdx = null;

        if (!companyName.isEmpty()) {
            companyIdx = companyRepository
                    .findByName(companyName)
                    .map(Company::getIdx)
                    .orElseGet(() -> {
                        Company c = new Company();
                        c.setName(companyName);
                        c.setCreated_at(LocalDateTime.now());
                        c.setUpdated_at(LocalDateTime.now());
                        return companyRepository.save(c).getIdx();
                    });
        }

        // 2) user_idx 검증
        Long finalUserId = null;
        if (user_idx != null && user_idx > 0L) {
            if (userRepository.existsById(user_idx)) {
                finalUserId = user_idx;
            }
        }

        // 3) Placeholder user 자동 생성
        if (finalUserId == null) {

            Users u = new Users();

            u.setUserId("auto_" + UUID.randomUUID().toString().substring(0, 8));
            u.setPassword("TEMP_PASSWORD"); // 암호화 안함 (실사용 X)
            u.setName("auto_generated");
            u.setEmail("auto_" + UUID.randomUUID().toString().substring(0, 5) + "@nomail.com");
            u.setVerified(false);
            u.setCreated_at(LocalDateTime.now());
            u.setUpdated_at(LocalDateTime.now());

            Users savedUser = userRepository.save(u);
            finalUserId = savedUser.getIdx();

            System.out.println("✨ Placeholder user 생성됨 → user.id = " + finalUserId);
        }

        // 4) 명함 저장
        BizCard card = new BizCard();
        card.setUser_idx(finalUserId);
        card.setName(nvl(data.get("name")));
        card.setCompany_idx(companyIdx != null ? companyIdx : 0L);
        card.setDepartment(nvl(data.get("department")));
        card.setPosition(nvl(data.get("position")));
        card.setEmail(nvl(data.get("email")));
        card.setPhone_number(nvl(data.get("mobile")));
        card.setLine_number(nvl(data.get("tel")));
        card.setFax_number(nvl(data.get("fax")));
        card.setAddress(nvl(data.get("address")));
        card.setMemo("");
        card.setCreated_at(LocalDateTime.now());
        card.setUpdated_at(LocalDateTime.now());

        return bizCardRepository.save(card);
    }

    //================ 유틸 ==================

    private static String buildPayloadWithFile(String filePath) throws IOException {
        File f = new File(filePath);
        byte[] img = Files.readAllBytes(f.toPath());
        String b64 = Base64.getEncoder().encodeToString(img);
        String ext = getExt(filePath);
        return "{"
                + "\"version\":\"V2\","
                + "\"requestId\":\"" + UUID.randomUUID() + "\","
                + "\"timestamp\":" + System.currentTimeMillis() + ","
                + "\"lang\":\"ko\","
                + "\"images\":[{"
                + "\"format\":\"" + (ext.isEmpty() ? "jpg" : ext) + "\","
                + "\"name\":\"bizcard\","
                + "\"data\":\"" + b64 + "\""
                + "}]"
                + "}";
    }

    private static String readAll(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        for (String line; (line = br.readLine()) != null;) {
            sb.append(line).append('\n');
        }
        return sb.toString();
    }

    private static String getExt(String path) {
        int i = path.lastIndexOf('.');
        return (i >= 0) ? path.substring(i + 1).toLowerCase() : "";
    }

    private static String extractFirstText(String json, String regex) {
        Pattern p = Pattern.compile(regex, Pattern.DOTALL);
        Matcher m = p.matcher(json);
        return m.find() ? unescape(m.group(1)) : null;
    }

    private static String unescape(String s) {
        if (s == null) return null;
        return s.replace("\\\"", "\"");
    }

    private static String firstNonNull(String... vals) {
        for (String v : vals) if (v != null && !v.isEmpty()) return v;
        return null;
    }

    private static String val(String s) {
        return (s == null) ? "" : s;
        }

    private String nvl(String s) {
        return (s == null) ? "" : s;
    }
}
