package com.bbey.neez.service;

import com.bbey.neez.entity.BizCard;
import com.bbey.neez.repository.BizCardReaderRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class BizCardReaderServiceImpl implements BizCardReaderService {

    // API Gateway URL과 SECRET은 application.properties 또는 환경변수로 관리합니다.
    @Value("${ocr.apigw.url:https://example-api-gateway.local/ocr}")
    private String APIGW_URL;

    @Value("${ocr.apigw.secret:REPLACE_WITH_REAL_SECRET}")
    private String SECRET;

    @Autowired
    private BizCardReaderRepository bizCardReaderRepository;

    @Override
    public String readBizCard() {
        try {
            // 리소스 경로는 프로젝트 루트에서의 상대 경로로 지정
            // String payload = buildPayloadWithFile("src/main/resources/BizCard/jasonLee.jpg");
            String payload = buildPayloadWithFile("src/main/resources/BizCard/SeoInMun.png");

            // 디버깅: data 들어갔는지 확인
            System.out.println("=== 요청 JSON ===\n" + payload);
            System.out.println("has data? " + payload.contains("\"data\""));

            // 로컬 개발 시 기본 placeholder URL이 사용중이면 외부 호출을 수행하지 않고 모의 응답을 반환합니다.
            if (APIGW_URL != null && APIGW_URL.contains("example-api-gateway")) {
                System.out.println("APIGW_URL이 placeholder로 설정되어 있어 외부 호출을 건너뜁니다. 모의 응답을 반환합니다.");
                return "{\"mock\":\"ok\"}";
            }

            // HTTP POST
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

            System.out.println("\n=== HTTP Status === " + code);
            System.out.println("=== 원본 응답(JSON) ===\n" + resp);

            // 결과 저장
            try (FileOutputStream fos = new FileOutputStream("result.json")) {
                fos.write(resp.getBytes("UTF-8"));
            }
            System.out.println("\n✔ result.json 저장 완료");

            // ✅ 콘솔에 명함 정보 출력
            printNameCardToConsole(resp);

            return resp;

        } catch (Exception e) {
            e.printStackTrace();
            return "error: " + e.getClass().getSimpleName() + " - " + e.getMessage();
        }
    }



    // ── 콘솔 출력: 회사/이름/부서/직책/전화/휴대폰/팩스/이메일/주소 ─────────────

    private static void printNameCardToConsole(String json) {
//    	Clova OCR 응답 JSON 문자열에서 필요한 값들을 정규식으로 추출해 출력
        String name       = extractFirstText(json, "\"name\"\\s*:\\s*\\[\\s*\\{[\\s\\S]*?\"text\"\\s*:\\s*\"(.*?)\"");
        String department = extractFirstText(json, "\"department\"\\s*:\\s*\\[\\s*\\{[\\s\\S]*?\"text\"\\s*:\\s*\"(.*?)\"");
        String position   = extractFirstText(json, "\"position\"\\s*:\\s*\\[\\s*\\{[\\s\\S]*?\"text\"\\s*:\\s*\"(.*?)\"");
        String mobile     = extractFirstText(json, "\"mobile\"\\s*:\\s*\\[\\s*\\{[\\s\\S]*?\"text\"\\s*:\\s*\"(.*?)\"");
        String tel        = extractFirstText(json, "\"tel\"\\s*:\\s*\\[\\s*\\{[\\s\\S]*?\"text\"\\s*:\\s*\"(.*?)\"");
        String email      = extractFirstText(json, "\"email\"\\s*:\\s*\\[\\s*\\{[\\s\\S]*?\"text\"\\s*:\\s*\"(.*?)\"");
        String address    = extractFirstText(json, "\"address\"\\s*:\\s*\\[\\s*\\{[\\s\\S]*?\"text\"\\s*:\\s*\"(.*?)\"");

        // ✅ fax: "fax" / "faxNo" 모두 대비
        String fax        = firstNonNull(
                extractFirstText(json, "\"fax\"\\s*:\\s*\\[\\s*\\{[\\s\\S]*?\"text\"\\s*:\\s*\"(.*?)\""),
                extractFirstText(json, "\"faxNo\"\\s*:\\s*\\[\\s*\\{[\\s\\S]*?\"text\"\\s*:\\s*\"(.*?)\"")
        );

        // ✅ company(회사명): 다양한 키 가능성 대비
        String company    = firstNonNull(
                extractFirstText(json, "\"company\"\\s*:\\s*\\[\\s*\\{[\\s\\S]*?\"text\"\\s*:\\s*\"(.*?)\""),
                extractFirstText(json, "\"companyName\"\\s*:\\s*\\[\\s*\\{[\\s\\S]*?\"text\"\\s*:\\s*\"(.*?)\""),
                extractFirstText(json, "\"organization\"\\s*:\\s*\\[\\s*\\{[\\s\\S]*?\"text\"\\s*:\\s*\"(.*?)\""),
                extractFirstText(json, "\"org\"\\s*:\\s*\\[\\s*\\{[\\s\\S]*?\"text\"\\s*:\\s*\"(.*?)\""),
                extractFirstText(json, "\"corp\"\\s*:\\s*\\[\\s*\\{[\\s\\S]*?\"text\"\\s*:\\s*\"(.*?)\""),
                extractFirstText(json, "\"office\"\\s*:\\s*\\[\\s*\\{[\\s\\S]*?\"text\"\\s*:\\s*\"(.*?)\"")
        );

        System.out.println("\n=== 추출 결과(요약) ===");
        System.out.println("회사명    : " + val(company));
        System.out.println("이름      : " + val(name));
        System.out.println("부서      : " + val(department));
        System.out.println("직책      : " + val(position));
        System.out.println("전화(회사) : " + val(tel));
        System.out.println("휴대폰    : " + val(mobile));
        System.out.println("팩스      : " + val(fax));
        System.out.println("이메일    : " + val(email));
        System.out.println("주소      : " + val(address));

        // 디버그 도움: 전부 못 찾았으면 nameCard 블록 일부를 잘라 보여줌
        if (allEmpty(company, name, department, position, tel, mobile, fax, email, address)) {
            int i = json.indexOf("\"nameCard\"");
            int j = json.indexOf("\"images \"");
            String snippet = (i >= 0)
                    ? json.substring(i, Math.min(json.length(), i + 1200))
                    : json.substring(0, Math.min(json.length(), 1200));
            System.out.println("\n[디버그] 매칭 실패 — 응답 일부 샘플:\n" + snippet);
        }
    }

    private static boolean allEmpty(String... vals) {
//    	전달된 문자열들 중 비어있지 않은 값이 하나라도 있는지 확인.
        for (String v : vals) {
            if (v != null && !v.isEmpty()) return false;
        }
        return true;
    }

    // 여러 후보 중 첫 번째로 발견된 값 반환
    private static String firstNonNull(String... vals) {
        for (String v : vals) if (v != null && !v.isEmpty()) return v;
        return null;
    }

    // 값이 없으면 보기 좋게 빈 문자열
    private static String val(String s) {
        return (s == null || s.isEmpty()) ? "" : s;
    }

    // JSON 문자열에서 정규식 그룹 1에 매칭된 첫 텍스트 추출 (멀티라인 허용)
    private static String extractFirstText(String json, String regex) {
        Pattern p = Pattern.compile(regex, Pattern.DOTALL | Pattern.UNICODE_CASE);
        Matcher m = p.matcher(json);
        return m.find() ? unescape(m.group(1)) : null;
    }


    private static String unescape(String s) {
//    	JSON 문자열 내부의 이스케이프 문자(예: \", \\, \n, \t)와 유니코드 이스케이프를 실제 문자로 변환.
//		정규식 추출 결과가 이스케이프된 경우 사람이 읽기 좋은 값으로 복원
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
                // 일반 이스케이프 처리
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

    // ── 요청 페이로드/IO 유틸 ───────────────────────────────────────

    private static String buildPayloadWithFile(String filePath) throws IOException {
        File f = new File(filePath);
        if (!f.exists() || !f.isFile()) {
            throw new FileNotFoundException("이미지 파일을 찾을 수 없습니다: " + f.getAbsolutePath());
        }
        if (f.length() <= 0) {
            throw new IOException("이미지 파일 크기가 0바이트입니다: " + f.getAbsolutePath());
        }

        byte[] img = Files.readAllBytes(f.toPath());
        if (img.length <= 0) {
            throw new IOException("이미지 바이너리 읽기 실패: " + f.getAbsolutePath());
        }

        String b64 = Base64.getEncoder().encodeToString(img);
        String ext = getExt(filePath); // jpg/png/tiff 등

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

    @SuppressWarnings("unused")
    private static String buildPayloadWithUrl(String imageUrl) {
//    	(현재 엔드포인트에서 URL 방식 미사용 가정)
//    	이미지 URL을 직접 전달하는 요청 JSON을 만드는 대안 메서드.
        return "{"
                + "\"version\":\"V2\","
                + "\"requestId\":\"" + UUID.randomUUID() + "\","
                + "\"timestamp\":" + System.currentTimeMillis() + ","
                + "\"lang\":\"ko\","
                + "\"images\":[{"
                +   "\"format\":\"jpg\","
                +   "\"name\":\"bizcard\","
                +   "\"url\":\"" + escape(imageUrl) + "\""
                + "}]"
                + "}";
    }

    private static String readAll(InputStream is) throws IOException {
//    	입력 스트림을 끝까지 읽어 하나의 문자열로 반환.
//    	HTTP 응답 본문을 문자열로 만들 때 사용.
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
            StringBuilder sb = new StringBuilder();
            for (String line; (line = br.readLine()) != null; ) sb.append(line).append('\n');
            return sb.toString();
        }
    }

    private static String getExt(String path) {
        int i = path.lastIndexOf('.');
        return (i >= 0) ? path.substring(i + 1).toLowerCase() : "";
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}