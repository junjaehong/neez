package com.bbey.neez.controller;

import com.bbey.neez.dto.ApiResponseDto;
import com.bbey.neez.dto.BizCardDto;
import com.bbey.neez.dto.MemoDto;
import com.bbey.neez.entity.BizCard;
import com.bbey.neez.entity.BizCardSaveResult;
import com.bbey.neez.service.BizCardReaderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/bizcards")   // ✅ prefix 통일
public class BizCardReaderController {

    private final BizCardReaderService bizCardReaderService;

    public BizCardReaderController(BizCardReaderService bizCardReaderService) {
        this.bizCardReaderService = bizCardReaderService;
    }

    // ✅ OCR → 저장
    @PostMapping("/read")
    public ResponseEntity<ApiResponseDto<BizCardDto>> ocrAndSave(@RequestBody Map<String, String> body) {
        try {
            String fileName = body.get("fileName");
            Long userIdx = body.containsKey("user_idx") ? Long.valueOf(body.get("user_idx")) : null;

            Map<String, String> ocrData = bizCardReaderService.readBizCard(fileName);
            BizCardSaveResult result = bizCardReaderService.saveBizCardFromOcr(ocrData, userIdx);

            // 회사명은 OCR 데이터에 있을 수도 있고 없을 수도 있음
            String companyName = ocrData.getOrDefault("company", null);

            BizCardDto dto = toBizCardDto(result.getBizCard(), companyName, null);

            return ResponseEntity.ok(
                    new ApiResponseDto<>(
                            true,
                            result.isExisting() ? "already exists" : "ok",
                            dto
                    )
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>(false, e.getMessage(), null));
        }
    }

    // ✅ 수기 등록
    @PostMapping("/manual")
    public ResponseEntity<ApiResponseDto<BizCardDto>> createManual(
            @RequestBody(required = false) Map<String, String> data
    ) {
        if (data == null) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponseDto<>(false, "JSON body is required", null));
        }

        try {
            Long userIdx = null;
            if (data.containsKey("user_idx") && data.get("user_idx") != null && !data.get("user_idx").isEmpty()) {
                userIdx = Long.valueOf(data.get("user_idx"));
            }

            BizCardSaveResult result = bizCardReaderService.saveManualBizCard(data, userIdx);
            BizCardDto dto = toBizCardDto(result.getBizCard(), data.get("company"), null);

            return ResponseEntity.ok(
                    new ApiResponseDto<>(
                            true,
                            result.isExisting() ? "already exists" : "ok",
                            dto
                    )
            );

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>(false, e.getMessage(), null));
        }
    }

    // ✅ 명함 하나 가져오기
    @GetMapping("/{idx}")
    public ResponseEntity<ApiResponseDto<BizCardDto>> getBizCard(@PathVariable Long idx) {
        try {
            // 서비스가 Map<String,Object>로 주는 걸 일단 받아서 DTO로 변환
            Map<String, Object> card = bizCardReaderService.getBizCardDetail(idx);

            BizCardDto dto = new BizCardDto(
                    (Long) card.get("idx"),
                    (Long) card.get("user_idx"),
                    (String) card.get("name"),
                    (String) card.get("company_name"),
                    (String) card.get("department"),
                    (String) card.get("position"),
                    (String) card.get("email"),
                    (String) card.get("phone_number"),
                    (String) card.get("line_number"),
                    (String) card.get("fax_number"),
                    (String) card.get("address"),
                    (String) card.get("memo_content")
            );

            return ResponseEntity.ok(new ApiResponseDto<>(true, "ok", dto));
        } catch (Exception e) {
            return ResponseEntity.status(404)
                    .body(new ApiResponseDto<>(false, e.getMessage(), null));
        }
    }

    // ✅ 명함 수정하기
    @PutMapping("/{idx}")
    public ResponseEntity<ApiResponseDto<BizCardDto>> updateBizCard(
            @PathVariable Long idx,
            @RequestBody Map<String, String> body
    ) {
        try {
            BizCard updated = bizCardReaderService.updateBizCard(idx, body);
            // 회사명은 여기선 모르니까 일단 null
            BizCardDto dto = toBizCardDto(updated, null, null);
            return ResponseEntity.ok(new ApiResponseDto<>(true, "updated", dto));
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(new ApiResponseDto<>(false, e.getMessage(), null));
        }
    }

    // ✅ 명함 메모만 가져오기
    @GetMapping("/{id}/memo")
    public ResponseEntity<ApiResponseDto<MemoDto>> getMemo(@PathVariable Long id) {
        try {
            String memoContent = bizCardReaderService.getBizCardMemoContent(id);
            MemoDto dto = new MemoDto(id, memoContent, "card-" + id + ".txt");
            return ResponseEntity.ok(new ApiResponseDto<>(true, "ok", dto));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponseDto<>(false, e.getMessage(), null));
        }
    }

    // ✅ 명함 메모만 수정하기
    @PatchMapping("/{id}/memo")
    public ResponseEntity<ApiResponseDto<MemoDto>> updateBizCardMemo(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        String memo = body.get("memo");
        if (memo == null) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponseDto<>(false, "memo is required", null));
        }

        BizCard updated = bizCardReaderService.updateBizCardMemo(id, memo);
        // DB에 저장된 파일명 다시 내려줌
        MemoDto dto = new MemoDto(id, memo, updated.getMemo());
        return ResponseEntity.ok(new ApiResponseDto<>(true, "memo updated", dto));
    }

    // ================== 헬퍼 ==================
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
