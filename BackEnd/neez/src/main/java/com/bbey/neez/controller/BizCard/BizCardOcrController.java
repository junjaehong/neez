package com.bbey.neez.controller.BizCard;

import com.bbey.neez.DTO.ApiResponseDto;
import com.bbey.neez.DTO.BizCardDto;
import com.bbey.neez.DTO.request.BizCardOcrRequest;
import com.bbey.neez.entity.BizCard;
import com.bbey.neez.entity.BizCardSaveResult;
import com.bbey.neez.service.BizCard.BizCardOcrService;
import com.bbey.neez.service.BizCard.BizCardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/bizcards")
@Tag(name = "BizCard OCR API", description = "명함 이미지 OCR → 명함/회사 자동 등록")
public class BizCardOcrController {

    private final BizCardOcrService bizCardOcrService;
    private final BizCardService bizCardService;

    public BizCardOcrController(BizCardOcrService bizCardOcrService,
                                BizCardService bizCardService) {
        this.bizCardOcrService = bizCardOcrService;
        this.bizCardService = bizCardService;
    }

    @Operation(
            summary = "명함 OCR 등록(서버 파일명)",
            description = "이미 서버에 저장된 명함 이미지 파일명을 기준으로 OCR → 명함/회사 자동 등록을 수행한다."
    )
    @PostMapping("/read")
    public ResponseEntity<ApiResponseDto<BizCardDto>> ocrAndSave(@RequestBody BizCardOcrRequest body) {
        try {
            String fileName = body.getFileName();
            Long userIdx = body.getUserIdx();

            Map<String, String> ocrData = bizCardOcrService.readBizCard(fileName);
            BizCardSaveResult result = bizCardService.saveFromOcrData(ocrData, userIdx);

            BizCard card = result.getBizCard();
            BizCardDto dto = new BizCardDto(
                    card.getIdx(),
                    card.getUserIdx(),
                    card.getName(),
                    card.getCardCompanyName(),
                    card.getCompanyIdx(),
                    card.getDepartment(),
                    card.getPosition(),
                    card.getEmail(),
                    card.getPhoneNumber(),
                    card.getLineNumber(),
                    card.getFaxNumber(),
                    card.getAddress(),
                    null,
                    null
            );

            return ResponseEntity.ok(
                    new ApiResponseDto<>(true, result.isExisting() ? "already exists" : "ok", dto)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>(false, e.getMessage(), null));
        }
    }

    @Operation(
            summary = "명함 이미지 업로드 + OCR (multipart/form-data)",
            description = "클라이언트에서 multipart/form-data로 명함 이미지를 업로드하면, 서버에서 저장 후 OCR → 명함/회사 자동 등록을 수행한다."
    )
    @PostMapping(value = "/read/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponseDto<BizCardDto>> uploadAndOcrMultipart(
            @Parameter(description = "명함 이미지 파일") @RequestPart("file") MultipartFile file,
            @Parameter(description = "명함 소유자 IDX", example = "1")
            @RequestParam(value = "user_idx", required = false) Long userIdx
    ) {
        try {
            String storedFileName = bizCardOcrService.storeBizCardImage(file);
            Map<String, String> ocrData = bizCardOcrService.readBizCard(storedFileName);
            BizCardSaveResult result = bizCardService.saveFromOcrData(ocrData, userIdx);

            BizCard card = result.getBizCard();
            BizCardDto dto = new BizCardDto(
                    card.getIdx(),
                    card.getUserIdx(),
                    card.getName(),
                    card.getCardCompanyName(),
                    card.getCompanyIdx(),
                    card.getDepartment(),
                    card.getPosition(),
                    card.getEmail(),
                    card.getPhoneNumber(),
                    card.getLineNumber(),
                    card.getFaxNumber(),
                    card.getAddress(),
                    null,
                    null
            );

            return ResponseEntity.ok(new ApiResponseDto<>(true, "ok", dto));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponseDto<>(false, e.getMessage(), null));
        }
    }

    @Operation(
            summary = "명함 이미지 업로드 + OCR (application/octet-stream)",
            description = "바이너리 바디(application/octet-stream)로 이미지를 보내면 서버에서 저장 후 OCR → 명함/회사 자동 등록을 수행한다."
    )
    @PostMapping(value = "/read/upload", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<ApiResponseDto<BizCardDto>> uploadAndOcrBytes(
            @RequestBody byte[] rawBytes,
            @RequestParam(value = "user_idx", required = false) Long userIdx
    ) {
        try {
            String storedFileName = bizCardOcrService.storeBizCardImage(rawBytes, "upload.jpg");
            Map<String, String> ocrData = bizCardOcrService.readBizCard(storedFileName);
            BizCardSaveResult result = bizCardService.saveFromOcrData(ocrData, userIdx);

            BizCard card = result.getBizCard();
            BizCardDto dto = new BizCardDto(
                    card.getIdx(),
                    card.getUserIdx(),
                    card.getName(),
                    card.getCardCompanyName(),
                    card.getCompanyIdx(),
                    card.getDepartment(),
                    card.getPosition(),
                    card.getEmail(),
                    card.getPhoneNumber(),
                    card.getLineNumber(),
                    card.getFaxNumber(),
                    card.getAddress(),
                    null,
                    null
            );

            return ResponseEntity.ok(new ApiResponseDto<>(true, "ok", dto));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponseDto<>(false, e.getMessage(), null));
        }
    }
}
