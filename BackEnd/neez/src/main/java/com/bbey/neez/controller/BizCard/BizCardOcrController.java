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

        @Operation(summary = "명함 OCR 등록(서버 파일명)", description = "이미 서버에 저장된 명함 이미지 파일명을 받아 OCR을 수행하고,\n" +
                        "명함 및 회사 정보를 자동으로 생성/연결한다.")
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
                                        null);

                        return ResponseEntity.ok(
                                        new ApiResponseDto<BizCardDto>(true,
                                                        result.isExisting() ? "already exists" : "ok", dto));
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(new ApiResponseDto<BizCardDto>(false, e.getMessage(), null));
                }
        }

        @Operation(summary = "명함 이미지 업로드 + OCR (multipart)", description = "클라이언트에서 multipart/form-data 로 명함 이미지를 업로드하면,\n"
                        +
                        "서버에서 이미지를 저장한 뒤 OCR을 수행하고,\n" +
                        "명함 및 회사 정보를 자동으로 생성/연결한다.")
        @PostMapping(value = "/read/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<ApiResponseDto<BizCardDto>> uploadAndOcrMultipart(
                        @RequestPart("file") MultipartFile file,
                        @RequestParam(value = "user_idx", required = false) Long userIdx) {
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
                                        null);

                        return ResponseEntity.ok(new ApiResponseDto<BizCardDto>(true, "ok", dto));
                } catch (Exception e) {
                        return ResponseEntity.status(500)
                                        .body(new ApiResponseDto<BizCardDto>(false, e.getMessage(), null));
                }
        }

        @Operation(summary = "명함 이미지 업로드 + OCR (octet-stream)", description = "클라이언트에서 application/octet-stream 으로 이미지를 전송하면,\n"
                        +
                        "서버에서 파일로 저장 후 OCR을 수행하고,\n" +
                        "명함 및 회사 정보를 자동으로 생성/연결한다.")
        @PostMapping(value = "/read/upload", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
        public ResponseEntity<ApiResponseDto<BizCardDto>> uploadAndOcrBytes(
                        @RequestBody byte[] rawBytes,
                        @RequestParam(value = "user_idx", required = false) Long userIdx) {
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
                                        null);

                        return ResponseEntity.ok(new ApiResponseDto<BizCardDto>(true, "ok", dto));
                } catch (Exception e) {
                        return ResponseEntity.status(500)
                                        .body(new ApiResponseDto<BizCardDto>(false, e.getMessage(), null));
                }
        }
}
