package com.bbey.neez.service;

import com.bbey.neez.DTO.BizCardDto;
import com.bbey.neez.component.MemoStorage;
import com.bbey.neez.entity.BizCard;
import com.bbey.neez.entity.BizCardSaveResult;
import com.bbey.neez.entity.Company;
import com.bbey.neez.entity.Users;
import com.bbey.neez.repository.BizCardRepository;
import com.bbey.neez.repository.CompanyRepository;
import com.bbey.neez.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Autowired
    private MemoStorage memoStorage;

    // 1) 명함 이미지(OCR) 분석
    @Override
    public Map<String, String> readBizCard(String fileName) {
        try {
            String payload = buildPayloadWithFile("src/main/resources/BizCard/" + fileName);

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

            // 디버깅용 저장
            try (FileOutputStream fos = new FileOutputStream("result.json")) {
                fos.write(resp.getBytes("UTF-8"));
            }

            return parseNameCardFromJson(resp);

        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    // 2) 수기 등록도 동일 로직 태움
    @Override
    public BizCardSaveResult saveManualBizCard(Map<String, String> data, Long userIdx) {
        return saveBizCardFromOcr(data, userIdx);
    }

    // 3) 실제 저장 로직
    @Override
    public BizCardSaveResult saveBizCardFromOcr(Map<String, String> data, Long user_idx) {
        // --- 회사 처리 ---
        String companyName = nvl(data.get("company"));
        Long companyIdx = null;
        if (!companyName.isEmpty()) {
            Optional<Company> opt = companyRepository.findByName(companyName);
            if (opt.isPresent()) {
                companyIdx = opt.get().getIdx();
            } else {
                Company c = new Company();
                c.setName(companyName);
                c.setCreated_at(LocalDateTime.now());
                c.setUpdated_at(LocalDateTime.now());
                companyIdx = companyRepository.save(c).getIdx();
            }
        }

        // --- 유저 처리 ---
        Long finalUserId;
        if (user_idx != null && user_idx > 0 && userRepository.existsById(user_idx)) {
            finalUserId = user_idx;
        } else {
            Users u = new Users();
            u.setName("auto_generated");
            u.setCreated_at(LocalDateTime.now());
            u.setUpdated_at(LocalDateTime.now());
            finalUserId = userRepository.save(u).getIdx();
        }

        // --- 중복 검사 (이름+이메일) ---
        String name  = nvl(data.get("name"));
        String email = nvl(data.get("email"));

        if (!name.isEmpty() && !email.isEmpty()) {
            Optional<BizCard> existedOpt = bizCardRepository.findByNameAndEmail(name, email);
            if (existedOpt.isPresent()) {
                return new BizCardSaveResult(existedOpt.get(), true);
            }
        }

        // --- 신규 엔티티 생성 ---
        BizCard card = new BizCard();
        card.setUserIdx(finalUserId);
        card.setName(name);
        card.setCompanyIdx(companyIdx);
        card.setDepartment(nvl(data.get("department")));
        card.setPosition(nvl(data.get("position")));
        card.setEmail(email);
        card.setPhoneNumber(nvl(data.get("mobile")));
        card.setLineNumber(nvl(data.get("tel")));
        card.setFaxNumber(nvl(data.get("fax")));
        card.setAddress(nvl(data.get("address")));
        card.setCreatedAt(LocalDateTime.now());
        card.setUpdatedAt(LocalDateTime.now());
        card.setIsDeleted(false);

        BizCard saved = bizCardRepository.save(card);

        // 메모 있을 때만 파일 저장
        String reqMemo = nvl(data.get("memo"));
        if (!reqMemo.isEmpty()) {
            String fileName = "card-" + saved.getIdx() + ".txt";
            try {
                memoStorage.write(fileName, reqMemo);
                saved.setMemo(fileName);
                saved.setUpdatedAt(LocalDateTime.now());
                saved = bizCardRepository.save(saved);
            } catch (IOException e) {
                System.out.println("메모 파일 처리 실패: " + e.getMessage());
            }
        }

        return new BizCardSaveResult(saved, false);
    }

    // 4) 명함 + 회사명 + 메모내용까지 묶어서 주는 메서드
    @Override
    public Map<String, Object> getBizCardDetail(Long id) {
        BizCard card = bizCardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("BizCard not found: " + id));

        String companyName = null;
        if (card.getCompanyIdx() != null && card.getCompanyIdx() > 0) {
            companyName = companyRepository.findById(card.getCompanyIdx())
                    .map(Company::getName)
                    .orElse(null);
        }

        // 메모 내용 읽기
        String memoContent = "";
        if (card.getMemo() != null && !card.getMemo().isEmpty()) {
            try {
                memoContent = memoStorage.read(card.getMemo());
            } catch (IOException e) {
                System.out.println("메모 파일 읽기 실패: " + e.getMessage());
                memoContent = "(메모 파일을 불러오는 중 오류가 발생했습니다)";
            }
        }

        Map<String, Object> cardMap = new LinkedHashMap<>();
        cardMap.put("idx", card.getIdx());
        cardMap.put("user_idx", card.getUserIdx());
        cardMap.put("name", card.getName());
        cardMap.put("company_idx", card.getCompanyIdx());
        cardMap.put("company_name", companyName);
        cardMap.put("department", card.getDepartment());
        cardMap.put("position", card.getPosition());
        cardMap.put("email", card.getEmail());
        cardMap.put("phone_number", card.getPhoneNumber());
        cardMap.put("line_number", card.getLineNumber());
        cardMap.put("fax_number", card.getFaxNumber());
        cardMap.put("address", card.getAddress());
        cardMap.put("memo_path", card.getMemo());
        cardMap.put("memo_content", memoContent);
        cardMap.put("created_at", card.getCreatedAt());
        cardMap.put("updated_at", card.getUpdatedAt());

        return cardMap;
    }

    // 5) 명함 정보만 수정
    @Override
    public BizCard updateBizCard(Long idx, Map<String, String> data) {
        BizCard card = bizCardRepository.findById(idx)
                .orElseThrow(() -> new RuntimeException("BizCard not found: " + idx));

        String name = data.get("name");
        if (name != null && !name.isEmpty()) {
            card.setName(name);
        }

        String companyIdxStr = data.get("company_idx");
        if (companyIdxStr != null && !companyIdxStr.isEmpty()) {
            card.setCompanyIdx(Long.valueOf(companyIdxStr));
        }

        String dept = data.get("department");
        if (dept != null) card.setDepartment(dept);

        String position = data.get("position");
        if (position != null) card.setPosition(position);

        String email = data.get("email");
        if (email != null) card.setEmail(email);

        String mobile = data.get("mobile");
        if (mobile != null) card.setPhoneNumber(mobile);

        String tel = data.get("tel");
        if (tel != null) card.setLineNumber(tel);

        String fax = data.get("fax");
        if (fax != null) card.setFaxNumber(fax);

        String address = data.get("address");
        if (address != null) card.setAddress(address);

        card.setUpdatedAt(LocalDateTime.now());
        return bizCardRepository.save(card);
    }

    // 6) 명함 메모만 따로 수정
    @Override
    public BizCard updateBizCardMemo(Long id, String memo) {
        BizCard card = bizCardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("BizCard not found: " + id));

        String fileName = "card-" + card.getIdx() + ".txt";

        try {
            memoStorage.write(fileName, memo);
            card.setMemo(fileName);
        } catch (Exception e) {
            System.out.println("memo update failed: " + e.getMessage());
        }

        card.setUpdatedAt(LocalDateTime.now());
        return bizCardRepository.save(card);
    }

    // 7) 메모 내용만 조회
    @Override
    public String getBizCardMemoContent(Long id) throws IOException {
        BizCard card = bizCardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("BizCard not found: " + id));

        if (card.getMemo() == null || card.getMemo().isEmpty()) {
            return "";
        }
        return memoStorage.read(card.getMemo());
    }

    // 8) UserIdx에 해당하는 명함들 페이징으로 가져오기 (삭제 안 된 것만)
    @Override
    public Page<BizCardDto> getBizCardsByUserIdx(Long userIdx, Pageable pageable) {
        Page<BizCard> page = bizCardRepository.findByUserIdxAndIsDeletedFalse(userIdx, pageable);

        return page.map(card -> {
            String companyName = null;
            if (card.getCompanyIdx() != null) {
                companyName = companyRepository.findById(card.getCompanyIdx())
                        .map(Company::getName)
                        .orElse(null);
            }
            String memoContent = "";
            if (card.getMemo() != null && !card.getMemo().isEmpty()) {
                try {
                    memoContent = memoStorage.read(card.getMemo());
                } catch (Exception ignored) {}
            }
            return new BizCardDto(
                    card.getIdx(),
                    card.getUserIdx(),
                    card.getName(),
                    companyName,
                    card.getDepartment(),
                    card.getPosition(),
                    card.getEmail(),
                    card.getPhoneNumber(),
                    card.getLineNumber(),
                    card.getFaxNumber(),
                    card.getAddress(),
                    memoContent
            );
        });
    }

    // 9) 소프트 삭제
    @Override
    public void deleteBizCard(Long id) {
        BizCard card = bizCardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("BizCard not found: " + id));

        card.setIsDeleted(true);
        card.setUpdatedAt(LocalDateTime.now());

        bizCardRepository.save(card);
    }

    // 10) 명함 검색
    @Override
    public Page<BizCardDto> searchBizCards(Long userIdx, String keyword, Pageable pageable) {
        Page<BizCard> page = bizCardRepository.searchByKeyword(userIdx, keyword, pageable);

        return page.map(card -> {
            String companyName = null;
            if (card.getCompanyIdx() != null) {
                companyName = companyRepository.findById(card.getCompanyIdx())
                        .map(Company::getName)
                        .orElse(null);
            }
            String memoContent = "";
            if (card.getMemo() != null && !card.getMemo().isEmpty()) {
                try {
                    memoContent = memoStorage.read(card.getMemo());
                } catch (Exception ignored) {}
            }
            return new BizCardDto(
                    card.getIdx(),
                    card.getUserIdx(),
                    card.getName(),
                    companyName,
                    card.getDepartment(),
                    card.getPosition(),
                    card.getEmail(),
                    card.getPhoneNumber(),
                    card.getLineNumber(),
                    card.getFaxNumber(),
                    card.getAddress(),
                    memoContent
            );
        });
    }

    // ========================= 유틸 =========================
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
                extractFirstText(json, "\"organization\"\\s*:\\s*\\[\\s*\\{[\\s\\S]*?\"text\"\\s*:\\*\"(.*?)\""),
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
                switch (n) {
                    case '"': case '\\': case '/':
                        out.append(n); i++; continue;
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
