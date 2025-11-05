package com.bbey.neez.service;

import com.bbey.neez.entity.BizCard;
import com.bbey.neez.entity.Company;
import com.bbey.neez.entity.Users;
import com.bbey.neez.repository.BizCardRepository;
import com.bbey.neez.repository.CompanyRepository;
import com.bbey.neez.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.bbey.neez.entity.BizCardSaveResult;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.charset.StandardCharsets;
import java.io.IOException;

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
        try {
            String payload = buildPayloadWithFile("src/main/resources/BizCard/" + fileName);

            // placeholder면 모의 응답
            if (APIGW_URL != null && APIGW_URL.contains("example-api-gateway")) {
                Map<String, String> mockMap = new LinkedHashMap<>();
                mockMap.put("company", "");
                mockMap.put("name", "");
                mockMap.put("department", "");
                mockMap.put("position", "");
                mockMap.put("tel", "");
                mockMap.put("mobile", "");
                mockMap.put("fax", "");
                mockMap.put("email", "");
                mockMap.put("address", "");
                return mockMap;
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

            try (FileOutputStream fos = new FileOutputStream("result.json")) {
                fos.write(resp.getBytes("UTF-8"));
            }

            return parseNameCardFromJson(resp);

        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    @Override
    public BizCardSaveResult saveBizCardFromOcr(Map<String, String> data, Long user_idx) {
        // 1. 회사 처리
        String companyName = nvl(data.get("company"));
        Long companyIdx = null;
        if (!companyName.isEmpty()) {
            // 람다 안 씀
            java.util.Optional<Company> opt = companyRepository.findByName(companyName);
            if (opt.isPresent()) {
                companyIdx = opt.get().getIdx();
            } else {
                Company c = new Company();
                c.setName(companyName);
                c.setCreated_at(java.time.LocalDateTime.now());
                c.setUpdated_at(java.time.LocalDateTime.now());
                companyIdx = companyRepository.save(c).getIdx();
            }
        }

        // 2. 사용자 처리
        Long finalUserId;
        if (user_idx != null && user_idx > 0 && userRepository.existsById(user_idx)) {
            finalUserId = user_idx;
        } else {
            com.bbey.neez.entity.Users u = new com.bbey.neez.entity.Users();
            u.setName("auto_generated");
            u.setCreated_at(java.time.LocalDateTime.now());
            u.setUpdated_at(java.time.LocalDateTime.now());
            finalUserId = userRepository.save(u).getIdx();
        }

        // 3. 중복 명함 체크 (이름 + 이메일)
        String name  = nvl(data.get("name"));
        String email = nvl(data.get("email"));

        if (!name.isEmpty() && !email.isEmpty()) {
            java.util.Optional<BizCard> existedOpt = bizCardRepository.findByNameAndEmail(name, email);
            if (existedOpt.isPresent()) {
                // 이미 있던 명함이면 그대로 리턴
                return new BizCardSaveResult(existedOpt.get(), true);
            }
        }

        // 4. 새 명함 생성
        BizCard card = new BizCard();
        card.setUserIdx(finalUserId);
        card.setName(name);
        card.setCompanyIdx(companyIdx); // null이면 null로 감
        card.setDepartment(nvl(data.get("department")));
        card.setPosition(nvl(data.get("position")));
        card.setEmail(email);
        card.setPhoneNumber(nvl(data.get("mobile"))); // 휴대폰
        card.setLineNumber(nvl(data.get("tel")));     // 회사번호
        card.setFaxNumber(nvl(data.get("fax")));
        card.setAddress(nvl(data.get("address")));
        card.setCreatedAt(java.time.LocalDateTime.now());
        card.setUpdatedAt(java.time.LocalDateTime.now());

        // 5. 메모 파일 처리 (DB에는 경로만)
        String reqMemo = nvl(data.get("memo"));     // 요청으로 온 메모
        String nameForFile = nvl(data.get("name")); // 파일명으로 쓸 우선값

        // 이름이 비어 있으면 user 기반으로라도 파일명 만들어서 아예 안 빠지게
        if (nameForFile.isEmpty()) {
            nameForFile = "user-" + finalUserId;
        }

        // 파일 경로를 항상 하나 만든다
        java.nio.file.Path memoPath = java.nio.file.Paths.get(
                "src", "main", "resources", "Memo", nameForFile + ".txt"
        );

        String memoToStoreInDb = "";  // 결국 DB에 넣을 값

        try {
            // 디렉터리 없으면 만들기
            if (memoPath.getParent() != null && !java.nio.file.Files.exists(memoPath.getParent())) {
                java.nio.file.Files.createDirectories(memoPath.getParent());
            }

            // 파일이 없고, 요청 메모가 있으면 새로 만들고 내용 쓰기
            if (!java.nio.file.Files.exists(memoPath)) {
                if (!reqMemo.isEmpty()) {
                    java.nio.file.Files.write(
                            memoPath,
                            (reqMemo + System.lineSeparator()).getBytes(java.nio.charset.StandardCharsets.UTF_8),
                            java.nio.file.StandardOpenOption.CREATE
                    );
                } else {
                    // 메모가 아예 없으면 빈 파일이라도 만들 수 있음 (선택)
                    java.nio.file.Files.write(
                            memoPath,
                            new byte[0],
                            java.nio.file.StandardOpenOption.CREATE
                    );
                }
            } else {
                // 파일이 이미 있으면, 요청에 메모가 있을 때만 이어붙이기
                if (!reqMemo.isEmpty()) {
                    String contentsToAppend = System.lineSeparator() + reqMemo + System.lineSeparator();
                    java.nio.file.Files.write(
                            memoPath,
                            contentsToAppend.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                            java.nio.file.StandardOpenOption.APPEND
                    );
                }
            }

            // 여기까지 오면 DB에는 경로만 저장
            memoToStoreInDb = memoPath.toString();

        } catch (java.io.IOException e) {
            System.out.println("메모 파일 처리 중 오류: " + e.getMessage());
            memoToStoreInDb = ""; // 실패했으면 빈 값
        }

        // 최종적으로 DB에는 경로만
        card.setMemo(memoToStoreInDb);

        BizCard saved = bizCardRepository.save(card);
        return new BizCardSaveResult(saved, false);
    }

    // 수기 등록
    @Override
    public BizCardSaveResult saveManualBizCard(Map<String, String> data, Long user_idx) {
        return saveBizCardFromOcr(data, user_idx);
    }


    // ============================================================================================================================
    // ============================================================================================================================
    // =============================================== 아래는 기존 유틸 ============================================================
    // ============================================================================================================================
    // ============================================================================================================================

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

        if (name != null && !name.matches(".*[a-zA-Z].*")) {
            name = name.replaceAll("\\s+", "");
        }

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
