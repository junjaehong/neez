package com.bbey.neez.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.LinkedHashMap;
import com.bbey.neez.service.BizCardReaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.Map;
import com.bbey.neez.entity.BizCard;

@RestController
@RequestMapping("/bizcard")
public class BizCardReaderController {

    @Autowired
    private BizCardReaderService bizCardReaderService;

    @RequestMapping("/read/{fileName}")
    public ResponseEntity<Map<String, Object>> ocrAndSave(
            @PathVariable String fileName,
            @RequestParam(value = "user_idx", required=false) Long user_idx) {

        Map<String, Object> resp = new LinkedHashMap<>();
        try {
            // 1. OCR → 데이터 추출
            Map<String, String> data = bizCardReaderService.readBizCard(fileName);

            // 2. DB 저장
            BizCard saved = bizCardReaderService.saveBizCardFromOcr(data, user_idx);

            boolean userAttached = saved != null && saved.getUser_idx() != null;

            // 3. 응답 구조 개선
            resp.put("success", true);
            resp.put("user_attached", userAttached);
            resp.put("ocr_data", data);   // OCR로 뽑은 원본 필드
            resp.put("card", saved);      // 실제 DB에 저장된 엔티티
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            resp.put("success", false);
            resp.put("error", e.getClass().getSimpleName());
            resp.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }

}