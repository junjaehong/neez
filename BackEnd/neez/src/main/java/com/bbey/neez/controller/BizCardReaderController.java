package com.bbey.neez.controller;

import com.bbey.neez.entity.BizCard;
import com.bbey.neez.service.BizCardReaderService;
import com.bbey.neez.entity.BizCardSaveResult;
import com.bbey.neez.repository.CompanyRepository;
import com.bbey.neez.component.MemoStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/bizcard")
public class BizCardReaderController {

    @Autowired
    private BizCardReaderService bizCardReaderService;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private MemoStorage memoStorage;

    // OCR → 저장
    @PostMapping("/read")
    public ResponseEntity<Map<String, Object>> ocrAndSave(@RequestBody Map<String, String> body) {
        Map<String, Object> resp = new LinkedHashMap<>();
        try {
            String fileName = body.get("fileName");
            Long userIdx = body.containsKey("user_idx") ? Long.valueOf(body.get("user_idx")) : null;

            Map<String, String> data = bizCardReaderService.readBizCard(fileName);
            BizCardSaveResult result = bizCardReaderService.saveBizCardFromOcr(data, userIdx);

            resp.put("success", true);
            resp.put("existing", result.isExisting());
            resp.put("ocr_data", data);
            resp.put("card", result.getBizCard());
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            resp.put("success", false);
            resp.put("error", e.getClass().getSimpleName());
            resp.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }

    // 수기 등록
    @PostMapping("/manual")
    public ResponseEntity<Map<String, Object>> createManual(@RequestBody(required = false) Map<String, String> data) {
        Map<String, Object> resp = new LinkedHashMap<>();
        try {
            if (data == null) {
                resp.put("success", false);
                resp.put("message", "JSON body is required");
                return ResponseEntity.badRequest().body(resp);
            }

            Long userIdx = null;
            if (data.containsKey("user_idx") && data.get("user_idx") != null && !data.get("user_idx").isEmpty()) {
                userIdx = Long.valueOf(data.get("user_idx"));
            }

            BizCardSaveResult result = bizCardReaderService.saveManualBizCard(data, userIdx);

            resp.put("success", true);
            resp.put("existing", result.isExisting());
            resp.put("card", result.getBizCard());
            resp.put("input_data", data);
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            resp.put("success", false);
            resp.put("error", e.getClass().getSimpleName());
            resp.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }

    // 명함 하나 가져오기
    @GetMapping("/{idx}")
    public ResponseEntity<?> getBizCard(@PathVariable Long idx) {
        try {
            Map<String, Object> card = bizCardReaderService.getBizCardDetail(idx);
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("success", true);
            resp.put("card", card);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("success", false);
            err.put("message", e.getMessage());
            return ResponseEntity.status(404).body(err);
        }
    }


    // 명함 수정하기
    @PutMapping("/{idx}")
    public ResponseEntity<?> updateBizCard(
            @PathVariable Long idx,
            @RequestBody Map<String, String> body
    ) {
        try {
            BizCard updated = bizCardReaderService.updateBizCard(idx, body);
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("success", true);
            resp.put("card", updated);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("success", false);
            resp.put("message", e.getMessage());
            return ResponseEntity.status(400).body(resp);
        }
    }

    // 명함 메모만 가져오기
    @GetMapping("/{id}/memo")
    public ResponseEntity<?> getMemo(@PathVariable Long id) {
        Map<String, Object> resp = new LinkedHashMap<>();
        try {
            String memoContent = bizCardReaderService.getBizCardMemoContent(id);
            resp.put("success", true);
            resp.put("memo_content", memoContent);
            resp.put("memo_file", "card-" + id + "-memo.txt");
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("error", e.getMessage());
            return ResponseEntity.status(500).body(resp);
        }
    }


    // 명함 메모만 수정하기
    @PatchMapping("/{id}/memo")
    public ResponseEntity<?> updateBizCardMemo(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        String memo = body.get("memo");
        if (memo == null) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("success", false);
            err.put("message", "memo is required");
            return ResponseEntity.badRequest().body(err);
        }

        BizCard updated = bizCardReaderService.updateBizCardMemo(id, memo);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("card", updated);
        return ResponseEntity.ok(resp);
    }


}
