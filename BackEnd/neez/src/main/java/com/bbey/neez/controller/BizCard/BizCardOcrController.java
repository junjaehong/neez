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

import java.util.HashMap;
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

        @Operation(summary = "명함 OCR 등록(서버 파일명)", description = "이미 서버에 저장된 명함 이미지 파일명을 받아 OCR을 수행하고,\n" +
                        "명함 정보를 현재 로그인한 사용자 소유로 저장합니다.\n" +
                        "이 단계에서는 회사 정보(company_idx) 연결을 하지 않고,\n" +
                        "나중에 명함 수정 화면에서 회사 검색/선택 후 연결합니다.")
        @PostMapping("/read")
        public ResponseEntity<ApiResponseDto<BizCardDto>> ocrAndSave(
                        @RequestBody BizCardOcrRequest body) {
                try {
                        String fileName = body.getFileName();

                        // ✅ 이미지 OCR
                        Map<String, String> ocrData = bizCardOcrService.readBizCard(fileName);

                        // OCR 결과 + 요청의 memo 를 합쳐서 저장
                        Map<String, String> saveData = new HashMap<>(ocrData);
                        if (body.getMemo() != null && !body.getMemo().trim().isEmpty()) {
                                saveData.put("memo", body.getMemo().trim());
                        }

                        // ✅ 현재 로그인한 유저 기준으로 명함 저장
                        BizCardSaveResult result = bizCardService.saveFromOcrData(saveData);

                        BizCard card = result.getBizCard();
                        BizCardDto dto = new BizCardDto(
                                        card.getIdx(),
                                        card.getUserIdx(),
                                        card.getName(),
                                        card.getCardCompanyName(),
                                        card.getCompanyIdx(), // 현재는 null
                                        card.getDepartment(),
                                        card.getPosition(),
                                        card.getEmail(),
                                        card.getPhoneNumber(),
                                        card.getLineNumber(),
                                        card.getFaxNumber(),
                                        card.getAddress(),
                                        null, // memo_content는 별도 조회 API에서
                                        null // 해시태그는 여기서 안 건드림
                        );

                        return ResponseEntity.ok(
                                        new ApiResponseDto<>(
                                                        true,
                                                        result.isExisting() ? "already exists" : "ok",
                                                        dto));
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(new ApiResponseDto<>(false, e.getMessage(), null));
                }
        }

        @Operation(summary = "명함 이미지 업로드 + OCR (multipart)", description = "클라이언트에서 multipart/form-data 로 명함 이미지를 업로드하면,\n"
                        +
                        "서버에서 이미지를 저장한 뒤 OCR을 수행하고,\n" +
                        "명함 정보를 현재 로그인한 사용자 소유로 저장합니다.\n" +
                        "이 단계에서는 회사 연결을 하지 않습니다.")
        @PostMapping(value = "/read/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<ApiResponseDto<BizCardDto>> uploadAndOcrMultipart(
                        @RequestPart("file") MultipartFile file) {
                try {
                        // ✅ 파일 저장
                        String storedFileName = bizCardOcrService.storeBizCardImage(file);

                        // ✅ OCR 수행
                        Map<String, String> ocrData = bizCardOcrService.readBizCard(storedFileName);

                        // ✅ 현재 로그인 유저 기준으로 명함 저장
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
                                        new ApiResponseDto<>(
                                                        true,
                                                        result.isExisting() ? "already exists" : "ok",
                                                        dto));
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(new ApiResponseDto<>(false, e.getMessage(), null));
                }
        }

        @Operation(summary = "명함 이미지 업로드 + OCR (바이너리)", description = "클라이언트에서 application/octet-stream 으로 이미지를 전송하면,\n"
                        +
                        "서버에서 파일로 저장 후 OCR을 수행하고,\n" +
                        "명함 정보를 현재 로그인한 사용자 소유로 저장합니다.\n" +
                        "이 단계에서는 회사 연결을 하지 않습니다.")
        @PostMapping(value = "/read/upload", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
        public ResponseEntity<ApiResponseDto<BizCardDto>> uploadAndOcrBytes(
                        @RequestBody byte[] rawBytes) {
                try {
                        // ✅ 바이트 배열을 이미지 파일로 저장
                        String storedFileName = bizCardOcrService.storeBizCardImage(rawBytes, "upload.jpg");

                        // ✅ OCR 수행
                        Map<String, String> ocrData = bizCardOcrService.readBizCard(storedFileName);

                        // ✅ 현재 로그인 유저 기준으로 명함 저장
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
                                        new ApiResponseDto<>(
                                                        true,
                                                        result.isExisting() ? "already exists" : "ok",
                                                        dto));
                } catch (Exception e) {
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                        .body(new ApiResponseDto<>(false, e.getMessage(), null));
                }
        }
}
