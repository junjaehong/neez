package com.bbey.neez.controller.BizCard;

import com.bbey.neez.DTO.ApiResponseDto;
import com.bbey.neez.DTO.BizCardDto;
import com.bbey.neez.DTO.cardRequest.BizCardOcrRequest;
import com.bbey.neez.entity.BizCard.BizCard;
import com.bbey.neez.entity.BizCard.BizCardSaveResult;
import com.bbey.neez.service.BizCard.BizCardOcrService;
import com.bbey.neez.service.BizCard.BizCardService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@SecurityRequirement(name = "BearerAuth")
public class BizCardOcrController {

        private final BizCardOcrService bizCardOcrService;
        private final BizCardService bizCardService;

        public BizCardOcrController(BizCardOcrService bizCardOcrService,
                        BizCardService bizCardService) {
                this.bizCardOcrService = bizCardOcrService;
                this.bizCardService = bizCardService;
        }

        @Operation(summary = "명함 OCR 등록(서버 파일명)", description = "이미 서버에 저장된 명함 이미지 파일명을 받아 OCR을 수행하고,\n"
                        + "명함 및 회사 정보를 자동으로 생성/연결한다.\n"
                        + "※ user_idx 는 더 이상 클라이언트에서 넘기지 않고, JWT 토큰의 사용자 기준으로 저장됩니다.")
        @PostMapping("/read")
        public ResponseEntity<ApiResponseDto<BizCardDto>> ocrAndSave(
                        @org.springframework.web.bind.annotation.RequestBody BizCardOcrRequest body) {
                try {
                        String fileName = body.getFileName();

                        // ✅ 이미지 OCR
                        Map<String, String> ocrData = bizCardOcrService.readBizCard(fileName);

                        // ✅ 현재 로그인한 유저 기준으로 명함 저장 (/me와 동일 정책)
                        BizCardSaveResult result = bizCardService.saveFromOcrData(ocrData);

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
                                        new ApiResponseDto<BizCardDto>(
                                                        true,
                                                        result.isExisting() ? "already exists" : "ok",
                                                        dto));
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(new ApiResponseDto<BizCardDto>(false, e.getMessage(), null));
                }
        }

        @Operation(summary = "명함 이미지 업로드 + OCR (multipart)", description = "클라이언트에서 multipart/form-data 로 명함 이미지를 업로드하면,\n"
                        + "서버에서 이미지를 저장한 뒤 OCR을 수행하고,\n"
                        + "명함 및 회사 정보를 자동으로 생성/연결한다.\n"
                        + "※ user_idx 는 쿼리로 받지 않고, JWT 토큰의 사용자 기준으로 저장됩니다.")
        @PostMapping(value = "/read/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<ApiResponseDto<BizCardDto>> uploadAndOcrMultipart(
                        @RequestPart("file") MultipartFile file) {
                try {
                        // ✅ 파일 저장
                        String storedFileName = bizCardOcrService.storeBizCardImage(file);

                        // ✅ OCR 수행
                        Map<String, String> ocrData = bizCardOcrService.readBizCard(storedFileName);

                        // ✅ 현재 로그인 유저 기준으로 저장
                        BizCardSaveResult result = bizCardService.saveFromOcrData(ocrData);

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
                                        new ApiResponseDto<BizCardDto>(
                                                        true,
                                                        result.isExisting() ? "already exists" : "ok",
                                                        dto));
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(new ApiResponseDto<BizCardDto>(false, e.getMessage(), null));
                }
        }

        @Operation(summary = "명함 이미지 업로드 + OCR (바이너리)", description = "클라이언트에서 application/octet-stream 으로 이미지를 전송하면,\n"
                        + "서버에서 파일로 저장 후 OCR을 수행하고,\n"
                        + "명함 및 회사 정보를 자동으로 생성/연결한다.\n"
                        + "※ user_idx 는 쿼리로 받지 않고, JWT 토큰의 사용자 기준으로 저장됩니다.")
        @PostMapping(value = "/read/upload", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
        public ResponseEntity<ApiResponseDto<BizCardDto>> uploadAndOcrBytes(
                        @org.springframework.web.bind.annotation.RequestBody byte[] rawBytes) {
                try {
                        // ✅ 바이트 배열을 이미지 파일로 저장
                        String storedFileName = bizCardOcrService.storeBizCardImage(rawBytes, "upload.jpg");

                        // ✅ OCR 수행
                        Map<String, String> ocrData = bizCardOcrService.readBizCard(storedFileName);

                        // ✅ 현재 로그인 유저 기준으로 저장
                        BizCardSaveResult result = bizCardService.saveFromOcrData(ocrData);

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
                                        new ApiResponseDto<BizCardDto>(
                                                        true,
                                                        result.isExisting() ? "already exists" : "ok",
                                                        dto));
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(new ApiResponseDto<BizCardDto>(false, e.getMessage(), null));
                }
        }
}
