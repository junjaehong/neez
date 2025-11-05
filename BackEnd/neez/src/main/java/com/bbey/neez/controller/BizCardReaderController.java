package com.bbey.neez.controller;

import com.bbey.neez.entity.BizCard;
import com.bbey.neez.service.BizCardReaderService;
import com.bbey.neez.entity.BizCardSaveResult;
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
}
