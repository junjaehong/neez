package com.bbey.neez.controller;

import com.bbey.neez.DTO.ApiResponseDto;
import com.bbey.neez.DTO.BizCardDto;
import com.bbey.neez.DTO.MemoDto;
import com.bbey.neez.entity.BizCard;
import com.bbey.neez.entity.BizCardSaveResult;
import com.bbey.neez.service.BizCardReaderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/bizcards")
public class BizCardReaderController {

    private final BizCardReaderService bizCardReaderService;

    public BizCardReaderController(BizCardReaderService bizCardReaderService) {
        this.bizCardReaderService = bizCardReaderService;
    }

    // ✅ 1. OCR → 저장
    @PostMapping("/read")
    public ResponseEntity<ApiResponseDto<BizCardDto>> ocrAndSave(@RequestBody Map<String, String> body) {
        try {
            String fileName = body.get("fileName");
            Long userIdx = body.containsKey("user_idx") ? Long.valueOf(body.get("user_idx")) : null;

            Map<String, String> ocrData = bizCardReaderService.readBizCard(fileName);
            BizCardSaveResult result = bizCardReaderService.saveBizCardFromOcr(ocrData, userIdx);

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

    // ✅ 1-2. OCR → 저장 (파일 업로드 버전)
    @PostMapping("/read/upload")
    public ResponseEntity<ApiResponseDto<BizCardDto>> uploadAndOcr(
            @RequestPart("file") org.springframework.web.multipart.MultipartFile file,
            @RequestPart(value = "user_idx", required = false) Long userIdx
    ) {
        try {
            // 1) 파일을 서버에 저장
            String storedFileName = bizCardReaderService.storeBizCardImage(file);

            // 2) 기존 OCR 로직 재사용 (파일명만 넘겨줌)
            Map<String, String> ocrData = bizCardReaderService.readBizCard(storedFileName);
            BizCardSaveResult result = bizCardReaderService.saveBizCardFromOcr(ocrData, userIdx);

            String companyName = ocrData.getOrDefault("company", null);
            BizCardDto dto = new BizCardDto(
                    result.getBizCard().getIdx(),
                    result.getBizCard().getUserIdx(),
                    result.getBizCard().getName(),
                    companyName,
                    result.getBizCard().getDepartment(),
                    result.getBizCard().getPosition(),
                    result.getBizCard().getEmail(),
                    result.getBizCard().getPhoneNumber(),
                    result.getBizCard().getLineNumber(),
                    result.getBizCard().getFaxNumber(),
                    result.getBizCard().getAddress(),
                    null
            );

            return ResponseEntity.ok(new ApiResponseDto<>(true, "ok", dto));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ApiResponseDto<>(false, e.getMessage(), null));
        }
    }


    // ✅ 2. 수기 등록
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

    // ✅ 3. 명함 하나 가져오기
    @GetMapping("/{idx}")
    public ResponseEntity<ApiResponseDto<BizCardDto>> getBizCard(@PathVariable Long idx) {
        try {
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

    // ✅ 4. UserIdx에 해당하는 명함 페이징으로 가져오기
    @GetMapping("/user/{userIdx}/page")
    public ResponseEntity<ApiResponseDto<Page<BizCardDto>>> getBizCardsPage(
            @PathVariable Long userIdx,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<BizCardDto> result = bizCardReaderService.getBizCardsByUserIdx(userIdx, pageable);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "ok", result));
    }

    // ✅ 5. 명함 수정하기
    @PutMapping("/{idx}")
    public ResponseEntity<ApiResponseDto<BizCardDto>> updateBizCard(
            @PathVariable Long idx,
            @RequestBody Map<String, String> body
    ) {
        try {
            BizCard updated = bizCardReaderService.updateBizCard(idx, body);
            BizCardDto dto = toBizCardDto(updated, null, null);
            return ResponseEntity.ok(new ApiResponseDto<>(true, "updated", dto));
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(new ApiResponseDto<>(false, e.getMessage(), null));
        }
    }

    // ✅ 6. 명함 메모만 가져오기
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

    // ✅ 7. 명함 메모만 수정하기
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
        MemoDto dto = new MemoDto(id, memo, updated.getMemo());
        return ResponseEntity.ok(new ApiResponseDto<>(true, "memo updated", dto));
    }

    // ✅ 8. 명함 삭제하기 (소프트 삭제)
    @DeleteMapping("/{idx}")
    public ResponseEntity<ApiResponseDto<Void>> deleteBizCard(@PathVariable Long idx) {
        try {
            bizCardReaderService.deleteBizCard(idx);
            return ResponseEntity.ok(new ApiResponseDto<>(true, "deleted", null));
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(new ApiResponseDto<>(false, e.getMessage(), null));
        }
    }

    // ✅ 9. 명함 검색
    @GetMapping("/user/{userIdx}/search")
    public ResponseEntity<ApiResponseDto<Page<BizCardDto>>> search(
            @PathVariable Long userIdx,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<BizCardDto> result = bizCardReaderService.searchBizCards(userIdx, keyword, pageable);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "ok", result));
    }

    // ✅ 10. 명함 복원
    @PatchMapping("/{id}/restore")
    public ResponseEntity<ApiResponseDto<Void>> restoreBizCard(@PathVariable Long id) {
        try {
            bizCardReaderService.restoreBizCard(id);
            return ResponseEntity.ok(new ApiResponseDto<>(true, "restored", null));
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(new ApiResponseDto<>(false, e.getMessage(), null));
        }
    }

    // ✅ 11. 사용자 명함 개수
    @GetMapping("/user/{userIdx}/count")
    public ResponseEntity<ApiResponseDto<Long>> countBizCards(@PathVariable Long userIdx) {
        long count = bizCardReaderService.countBizCardsByUser(userIdx);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "ok", count));
    }

    // ✅ 12. 중복 확인 (name + email)
    @GetMapping("/user/{userIdx}/exists")
    public ResponseEntity<ApiResponseDto<Boolean>> existsBizCard(
            @PathVariable Long userIdx,
            @RequestParam String name,
            @RequestParam String email
    ) {
        boolean exists = bizCardReaderService.existsBizCard(userIdx, name, email);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "ok", exists));
    }

    // ✅ 13. 소프트 삭제된 명함 조회
    @GetMapping("/user/{userIdx}/deleted")
    public ResponseEntity<ApiResponseDto<Page<BizCardDto>>> getDeletedBizCards(
            @PathVariable Long userIdx,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<BizCardDto> result = bizCardReaderService.getDeletedBizCardsByUserIdx(userIdx, pageable);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "ok", result));
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
