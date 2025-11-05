package com.bbey.neez.controller;

import com.bbey.neez.entity.BizCard;
import com.bbey.neez.service.BizCardReaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.bbey.neez.entity.BizCardSaveResult;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/bizcard")
public class BizCardReaderController {

    @Autowired
    private BizCardReaderService bizCardReaderService;

    @RequestMapping("/read/{fileName}")
    public ResponseEntity<Map<String, Object>> ocrAndSave(@PathVariable String fileName, @RequestParam(value = "user_idx", required=false) Long user_idx) {
        Map<String, Object> resp = new LinkedHashMap<>();
        try {
            Map<String, String> data = bizCardReaderService.readBizCard(fileName);

            BizCardSaveResult result = bizCardReaderService.saveBizCardFromOcr(data, user_idx);
            BizCard saved = result.getBizCard();

            resp.put("success", true);
            resp.put("existing", result.isExisting());  // ✅ 이제 진짜값
            resp.put("ocr_data", data);
            resp.put("card", saved);
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            resp.put("success", false);
            resp.put("error", e.getClass().getSimpleName());
            resp.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }

}
