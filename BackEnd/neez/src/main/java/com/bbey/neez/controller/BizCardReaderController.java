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
    public ResponseEntity<Map<String, Object>> ocrAndSave(@PathVariable String fileName, @RequestParam(value = "user_idx", required=false) Long user_idx){
        Map<String, Object> resp = new LinkedHashMap<>();
        try {
            // 1. OCR해서 데이터 뽑기
            Map<String, String> data = bizCardReaderService.readBizCard(fileName);

            // 2. DB 저장
            BizCard saved = bizCardReaderService.saveBizCardFromOcr(data, user_idx);

            boolean userAttached = saved != null && saved.getUser_idx() != null && !String.valueOf(saved.getUser_idx()).trim().isEmpty();

            resp.put("success", true);
            resp.put("user_attached", userAttached);
            resp.put("card", saved);
            return ResponseEntity.status(HttpStatus.OK).body(resp);
        } catch (Exception e) {
            // 오류 처리
            resp.put("success", false);
            resp.put("error", e.getClass().getSimpleName());
            resp.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resp);
        }
    }
}