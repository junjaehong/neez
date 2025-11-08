package com.bbey.neez.controller;

import com.bbey.neez.DTO.ApiResponseDto;
import com.bbey.neez.DTO.BizCardDto;
import com.bbey.neez.DTO.MemoDto;
import com.bbey.neez.entity.BizCard;
import com.bbey.neez.entity.BizCardSaveResult;
import com.bbey.neez.service.BizCardReaderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.bbey.neez.DTO.request.BizCardOcrRequest;
import com.bbey.neez.DTO.request.BizCardManualRequest;
import com.bbey.neez.DTO.request.BizCardUpdateRequest;
import com.bbey.neez.DTO.request.BizCardMemoUpdateRequest;
import org.springframework.http.MediaType;


import java.util.Map;

@RestController
@RequestMapping("/api/bizcards")
@Tag(name = "BizCard API", description = "명함 관리 기능 (OCR 등록, 조회, 수정, 삭제, 복구 등)")
public class BizCardReaderController {

    private final BizCardReaderService bizCardReaderService;

    public BizCardReaderController(BizCardReaderService bizCardReaderService) {
        this.bizCardReaderService = bizCardReaderService;
    }

    // ✅ 1-1. OCR (파일명 버전)
    @Operation(
            summary = "명함 OCR 등록",
            description = "서버에 존재하는 명함 이미지 파일명을 기반으로 OCR 분석 후 명함 정보를 저장합니다."
    )
    @PostMapping("/read")
    public ResponseEntity<ApiResponseDto<BizCardDto>> ocrAndSave(@RequestBody BizCardOcrRequest body) {
        try {
            String fileName = body.getFileName();
            Long userIdx = body.getUserIdx();

            Map<String, String> ocrData = bizCardReaderService.readBizCard(fileName);
            BizCardSaveResult result = bizCardReaderService.saveBizCardFromOcr(ocrData, userIdx);

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


    // ✅ 1-2. OCR → 저장 (multipart 업로드)
    @Operation(
            summary = "명함 이미지 업로드 + OCR 등록",
            description = "이미지 파일을 업로드하고 OCR 분석 후 명함 정보를 자동으로 저장합니다."
    )
    @PostMapping(
        value = "/read/upload",
        consumes = { MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_OCTET_STREAM_VALUE }
    )
    public ResponseEntity<ApiResponseDto<BizCardDto>> uploadAndOcr(
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart(value = "user_idx", required = false) Long userIdx,
            @RequestBody(required = false) byte[] rawBytes  // octet-stream 일 때
    ) {
        try {
            String storedFileName;

            if (file != null) {
                // multipart 들어온 경우
                storedFileName = bizCardReaderService.storeBizCardImage(file);
            } else if (rawBytes != null) {
                // octet-stream 들어온 경우
                storedFileName = bizCardReaderService.storeBizCardImage(rawBytes, "upload.jpg");
            } else {
                return ResponseEntity.badRequest()
                        .body(new ApiResponseDto<>(false, "file is required", null));
            }

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
    @Operation(
            summary = "명함 수기 등록",
            description = "수기로 입력한 명함 정보를 JSON 형식으로 받아 저장합니다."
    )
    @PostMapping("/manual")
    public ResponseEntity<ApiResponseDto<BizCardDto>> createManual(@RequestBody BizCardManualRequest data) {
        try {
            Long userIdx = data.getUser_idx();

            // Map으로 넘기던 걸 바꿔야 하니 서비스에 DTO 버전 오버로드를 만들거나,
            // 임시로 Map 만들어서 넘겨도 됨
            Map<String, String> map = new java.util.HashMap<>();
            map.put("company", data.getCompany());
            map.put("name", data.getName());
            map.put("department", data.getDepartment());
            map.put("position", data.getPosition());
            map.put("email", data.getEmail());
            map.put("mobile", data.getMobile());
            map.put("tel", data.getTel());
            map.put("fax", data.getFax());
            map.put("address", data.getAddress());
            map.put("memo", data.getMemo());

            BizCardSaveResult result = bizCardReaderService.saveManualBizCard(map, userIdx);
            BizCardDto dto = toBizCardDto(result.getBizCard(), data.getCompany(), null);

            return ResponseEntity.ok(
                    new ApiResponseDto<>(true, result.isExisting() ? "already exists" : "ok", dto)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponseDto<>(false, e.getMessage(), null));
        }
    }


    // ✅ 3. 명함 하나 가져오기
    @Operation(summary = "명함 상세 조회", description = "명함의 상세정보 (회사명, 메모 내용 포함)를 조회합니다.")
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

    // ✅ 4. 사용자 명함 목록
    @Operation(summary = "사용자 명함 목록 조회", description = "특정 사용자의 명함 목록을 페이징 형태로 조회합니다.")
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

    // ✅ 5. 명함 수정
    @Operation(summary = "명함 정보 수정", description = "명함의 기본 정보를 수정합니다.")
    @PutMapping("/{idx}")
    public ResponseEntity<ApiResponseDto<BizCardDto>> updateBizCard(
            @PathVariable Long idx,
            @RequestBody BizCardUpdateRequest body
    ) {
        try {
            // 서비스는 Map<String,String> 받고 있으니까 여기서 변환
            Map<String, String> map = new java.util.HashMap<>();
            if (body.getName() != null) map.put("name", body.getName());
            if (body.getCompany_idx() != null) map.put("company_idx", body.getCompany_idx().toString());
            if (body.getDepartment() != null) map.put("department", body.getDepartment());
            if (body.getPosition() != null) map.put("position", body.getPosition());
            if (body.getEmail() != null) map.put("email", body.getEmail());
            if (body.getMobile() != null) map.put("mobile", body.getMobile());
            if (body.getTel() != null) map.put("tel", body.getTel());
            if (body.getFax() != null) map.put("fax", body.getFax());
            if (body.getAddress() != null) map.put("address", body.getAddress());

            BizCard updated = bizCardReaderService.updateBizCard(idx, map);
            BizCardDto dto = toBizCardDto(updated, null, null);
            return ResponseEntity.ok(new ApiResponseDto<>(true, "updated", dto));
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(new ApiResponseDto<>(false, e.getMessage(), null));
        }
    }

    // ✅ 6. 메모 조회
    @Operation(summary = "명함 메모 조회", description = "명함에 연결된 메모 파일 내용을 조회합니다.")
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

    // ✅ 7. 메모 수정
    @Operation(summary = "명함 메모 수정", description = "명함의 메모 내용을 수정합니다.")
    @PatchMapping("/{id}/memo")
    public ResponseEntity<ApiResponseDto<MemoDto>> updateBizCardMemo(
            @PathVariable Long id,
            @RequestBody BizCardMemoUpdateRequest body
    ) {
        String memo = body.getMemo();
        if (memo == null) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponseDto<>(false, "memo is required", null));
        }

        BizCard updated = bizCardReaderService.updateBizCardMemo(id, memo);
        MemoDto dto = new MemoDto(id, memo, updated.getMemo());
        return ResponseEntity.ok(new ApiResponseDto<>(true, "memo updated", dto));
    }


    // ✅ 8. 삭제
    @Operation(summary = "명함 삭제 (Soft Delete)", description = "명함을 실제 삭제하지 않고 is_deleted=true로 표시합니다.")
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

    // ✅ 9. 검색
    @Operation(summary = "명함 검색", description = "사용자 명함 중 이름, 이메일, 부서명으로 검색합니다.")
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

    // ✅ 10. 복구
    @Operation(summary = "명함 복구", description = "is_deleted=true 상태의 명함을 복구합니다.")
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

    // ✅ 11. 개수
    @Operation(summary = "명함 개수 조회", description = "특정 사용자의 전체 명함 개수를 조회합니다.")
    @GetMapping("/user/{userIdx}/count")
    public ResponseEntity<ApiResponseDto<Long>> countBizCards(@PathVariable Long userIdx) {
        long count = bizCardReaderService.countBizCardsByUser(userIdx);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "ok", count));
    }

    // ✅ 12. 중복 확인
    @Operation(summary = "명함 중복 여부 확인", description = "사용자의 명함 중 동일한 이름 + 이메일이 존재하는지 확인합니다.")
    @GetMapping("/user/{userIdx}/exists")
    public ResponseEntity<ApiResponseDto<Boolean>> existsBizCard(
            @PathVariable Long userIdx,
            @RequestParam String name,
            @RequestParam String email
    ) {
        boolean exists = bizCardReaderService.existsBizCard(userIdx, name, email);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "ok", exists));
    }

    // ✅ 13. 삭제된 명함 목록
    @Operation(summary = "삭제된 명함 조회 (휴지통)", description = "소프트 삭제된 명함 목록을 페이징 형태로 조회합니다.")
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
