package com.bbey.neez.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class BizCardOcrServiceImpl implements BizCardOcrService {

    @Value("${ocr.apigw.url:https://example-api-gateway.local/ocr}")
    private String APIGW_URL;

    @Value("${ocr.apigw.secret:REPLACE_WITH_REAL_SECRET}")
    private String SECRET;

    @Value("${upload.bizcard.dir:src/main/resources/BizCard}")
    private String uploadDir;

    @Override
    public Map<String, String> readBizCard(String fileName) {
        try {
            String fullPath = uploadDir + File.separator + fileName;
            String payload = buildPayloadWithFile(fullPath);

            // mock 모드
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

            // 디버깅용
            try (FileOutputStream fos = new FileOutputStream("result.json")) {
                fos.write(resp.getBytes("UTF-8"));
            }

            return parseNameCardFromJson(resp);

        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    @Override
    public String storeBizCardImage(MultipartFile file) throws IOException {
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.lastIndexOf('.') != -1) {
            ext = original.substring(original.lastIndexOf('.'));
        }
        String storedName = "biz_" + System.currentTimeMillis() + ext;

        File dest = new File(dir, storedName);
        file.transferTo(dest);

        return storedName;
    }

    @Override
    public String storeBizCardImage(byte[] bytes, String filename) throws IOException {
        Path dir = Paths.get(uploadDir);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        Path target = dir.resolve(filename);
        Files.write(target, bytes);
        return filename;
    }

    // ===== 아래는 OCR 파싱 유틸 =====
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
                + "\"format\":\"" + (ext.isEmpty() ? "jpg" : ext) + "\","
                + "\"name\":\"bizcard\","
                + "\"data\":\"" + b64 + "\""
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
                extractFirstText(json, "\"companyName\"\\s*:\\s*\\[\\s\\S]*?\"text\"\\s*:\\s*\"(.*?)\"")
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
                switch (n) {
                    case '"': case '\\': case '/': out.append(n); i++; continue;
                    case 'b': out.append('\b'); i++; continue;
                    case 'f': out.append('\f'); i++; continue;
                    case 'n': out.append('\n'); i++; continue;
                    case 'r': out.append('\r'); i++; continue;
                    case 't': out.append('\t'); i++; continue;
                }
            }
            out.append(c);
        }
        return out.toString();
    }

    private static String firstNonNull(String... vals) {
        for (String v : vals) {
            if (v != null && !v.isEmpty()) return v;
        }
        return null;
    }

    private static String val(String s) {
        return (s == null || s.isEmpty()) ? "" : s;
    }
}
