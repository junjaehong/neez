package com.bbey.neez.controller;

import com.bbey.neez.DTO.ApiResponseDto;
import com.bbey.neez.DTO.BizCardDto;
import com.bbey.neez.DTO.request.BizCardOcrRequest;
import com.bbey.neez.entity.BizCard;
import com.bbey.neez.entity.BizCardSaveResult;
import com.bbey.neez.service.BizCardOcrService;
import com.bbey.neez.service.BizCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/bizcards")
@Tag(name = "BizCard OCR API", description = "명함 이미지 OCR 등록 전용")
public class BizCardOcrController {

    private final BizCardOcrService bizCardOcrService;
    private final BizCardService bizCardService;

    public BizCardOcrController(BizCardOcrService bizCardOcrService,
                                BizCardService bizCardService) {
        this.bizCardOcrService = bizCardOcrService;
        this.bizCardService = bizCardService;
    }

    // ✅ 서버에 있는 파일명으로 OCR
    @Operation(summary = "명함 OCR 등록(서버 파일명)")
    @PostMapping("/read")
    public ResponseEntity<ApiResponseDto<BizCardDto>> ocrAndSave(@RequestBody BizCardOcrRequest body) {
        try {
            String fileName = body.getFileName();
            Long userIdx = body.getUserIdx();

            Map<String, String> ocrData = bizCardOcrService.readBizCard(fileName);
            BizCardSaveResult result = bizCardService.saveFromOcrData(ocrData, userIdx);

            String companyName = ocrData.getOrDefault("company", null);
            BizCardDto dto = toBizCardDto(result.getBizCard(), companyName, null);

            return ResponseEntity.ok(
                    new ApiResponseDto<>(true, result.isExisting() ? "already exists" : "ok", dto)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>(false, e.getMessage(), null));
        }
    }

    // ✅ multipart 업로드 + OCR
    @Operation(summary = "명함 이미지 업로드 + OCR (multipart)")
    @PostMapping(value = "/read/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDto<BizCardDto>> uploadAndOcrMultipart(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "user_idx", required = false) Long userIdx
    ) {
        try {
            String storedFileName = bizCardOcrService.storeBizCardImage(file);
            Map<String, String> ocrData = bizCardOcrService.readBizCard(storedFileName);
            BizCardSaveResult result = bizCardService.saveFromOcrData(ocrData, userIdx);

            String companyName = ocrData.getOrDefault("company", null);
            BizCardDto dto = toBizCardDto(result.getBizCard(), companyName, null);

            return ResponseEntity.ok(new ApiResponseDto<>(true, "ok", dto));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponseDto<>(false, e.getMessage(), null));
        }
    }

    // ✅ octet-stream 업로드 + OCR
    @Operation(summary = "명함 이미지 업로드 + OCR (octet-stream)")
    @PostMapping(value = "/read/upload", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<ApiResponseDto<BizCardDto>> uploadAndOcrBytes(
            @RequestBody byte[] rawBytes,
            @RequestParam(value = "user_idx", required = false) Long userIdx
    ) {
        try {
            String storedFileName = bizCardOcrService.storeBizCardImage(rawBytes, "upload.jpg");
            Map<String, String> ocrData = bizCardOcrService.readBizCard(storedFileName);
            BizCardSaveResult result = bizCardService.saveFromOcrData(ocrData, userIdx);

            String companyName = ocrData.getOrDefault("company", null);
            BizCardDto dto = toBizCardDto(result.getBizCard(), companyName, null);

            return ResponseEntity.ok(new ApiResponseDto<>(true, "ok", dto));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponseDto<>(false, e.getMessage(), null));
        }
    }

    private BizCardDto toBizCardDto(BizCard card, String companyName, String memoContent) {
        if (card == null) return null;
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
    }
}
