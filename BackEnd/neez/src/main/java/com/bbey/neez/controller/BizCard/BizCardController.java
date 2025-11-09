package com.bbey.neez.controller.BizCard;

import com.bbey.neez.DTO.ApiResponseDto;
import com.bbey.neez.DTO.BizCardDto;
import com.bbey.neez.DTO.request.BizCardManualRequest;
import com.bbey.neez.DTO.request.BizCardUpdateRequest;
import com.bbey.neez.entity.BizCard;
import com.bbey.neez.entity.BizCardSaveResult;
import com.bbey.neez.service.BizCard.BizCardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/bizcards")
@Tag(name = "BizCard CRUD API", description = "명함 수기 등록, 조회, 수정, 삭제, 복구, 검색")
public class BizCardController {

    private final BizCardService bizCardService;

    public BizCardController(BizCardService bizCardService) {
        this.bizCardService = bizCardService;
    }

    // ✅ 수기 등록
    @Operation(summary = "명함 수기 등록")
    @PostMapping("/manual")
    public ResponseEntity<ApiResponseDto<BizCardDto>> createManual(@RequestBody BizCardManualRequest data) {
        try {
            Long userIdx = data.getUser_idx();

            Map<String, String> map = new HashMap<>();
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

            BizCardSaveResult result = bizCardService.saveManual(map, userIdx);
            BizCardDto dto = toBizCardDto(result.getBizCard(), data.getCompany(), null);

            return ResponseEntity.ok(
                    new ApiResponseDto<>(true, result.isExisting() ? "already exists" : "ok", dto)
            );
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponseDto<>(false, e.getMessage(), null));
        }
    }

    // ✅ 단건 조회
    @Operation(summary = "명함 상세 조회")
    @GetMapping("/{idx}")
    public ResponseEntity<ApiResponseDto<BizCardDto>> getBizCard(@PathVariable Long idx) {
        try {
            Map<String, Object> card = bizCardService.getBizCardDetail(idx);
            List<String> tags = (List<String>) (card.get("tags") != null ? card.get("tags") : new ArrayList<>());
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
                    (String) card.get("memo_content"),
                    tags
            );
            return ResponseEntity.ok(new ApiResponseDto<>(true, "ok", dto));
        } catch (Exception e) {
            return ResponseEntity.status(404)
                    .body(new ApiResponseDto<>(false, e.getMessage(), null));
        }
    }

    // ✅ 사용자 명함 목록
    @Operation(summary = "사용자 명함 목록 조회")
    @GetMapping("/user/{userIdx}/page")
    public ResponseEntity<ApiResponseDto<Page<BizCardDto>>> getBizCardsPage(
            @PathVariable Long userIdx,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<BizCardDto> result = bizCardService.getBizCardsByUserIdx(userIdx, pageable);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "ok", result));
    }

    // ✅ 명함 수정
    @Operation(summary = "명함 정보 수정")
    @PutMapping("/{idx}")
    public ResponseEntity<ApiResponseDto<BizCardDto>> updateBizCard(
            @PathVariable Long idx,
            @RequestBody BizCardUpdateRequest body
    ) {
        try {
            Map<String, String> map = new HashMap<>();
            if (body.getName() != null) map.put("name", body.getName());
            if (body.getCompany_idx() != null) map.put("company_idx", body.getCompany_idx().toString());
            if (body.getDepartment() != null) map.put("department", body.getDepartment());
            if (body.getPosition() != null) map.put("position", body.getPosition());
            if (body.getEmail() != null) map.put("email", body.getEmail());
            if (body.getMobile() != null) map.put("mobile", body.getMobile());
            if (body.getTel() != null) map.put("tel", body.getTel());
            if (body.getFax() != null) map.put("fax", body.getFax());
            if (body.getAddress() != null) map.put("address", body.getAddress());

            BizCard updated = bizCardService.updateBizCard(idx, map);
            BizCardDto dto = toBizCardDto(updated, null, null);
            return ResponseEntity.ok(new ApiResponseDto<>(true, "updated", dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponseDto<>(false, e.getMessage(), null));
        }
    }

    // ✅ 삭제
    @Operation(summary = "명함 삭제")
    @DeleteMapping("/{idx}")
    public ResponseEntity<ApiResponseDto<Void>> deleteBizCard(@PathVariable Long idx) {
        try {
            bizCardService.deleteBizCard(idx);
            return ResponseEntity.ok(new ApiResponseDto<>(true, "deleted", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponseDto<>(false, e.getMessage(), null));
        }
    }

    // ✅ 복구
    @Operation(summary = "명함 복구")
    @PatchMapping("/{id}/restore")
    public ResponseEntity<ApiResponseDto<Void>> restoreBizCard(@PathVariable Long id) {
        try {
            bizCardService.restoreBizCard(id);
            return ResponseEntity.ok(new ApiResponseDto<>(true, "restored", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponseDto<>(false, e.getMessage(), null));
        }
    }

    // ✅ 검색
    @Operation(summary = "명함 검색")
    @GetMapping("/user/{userIdx}/search")
    public ResponseEntity<ApiResponseDto<Page<BizCardDto>>> search(
            @PathVariable Long userIdx,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<BizCardDto> result = bizCardService.searchBizCards(userIdx, keyword, pageable);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "ok", result));
    }

    // ✅ 삭제된 명함 목록
    @Operation(summary = "삭제된 명함 목록 조회")
    @GetMapping("/user/{userIdx}/deleted")
    public ResponseEntity<ApiResponseDto<Page<BizCardDto>>> getDeletedBizCards(
            @PathVariable Long userIdx,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<BizCardDto> result = bizCardService.getDeletedBizCardsByUserIdx(userIdx, pageable);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "ok", result));
    }

    // ✅ 개수
    @Operation(summary = "명함 개수 조회")
    @GetMapping("/user/{userIdx}/count")
    public ResponseEntity<ApiResponseDto<Long>> countBizCards(@PathVariable Long userIdx) {
        long count = bizCardService.countBizCardsByUser(userIdx);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "ok", count));
    }

    // ✅ 중복 확인
    @Operation(summary = "명함 중복 확인")
    @GetMapping("/user/{userIdx}/exists")
    public ResponseEntity<ApiResponseDto<Boolean>> existsBizCard(
            @PathVariable Long userIdx,
            @RequestParam String name,
            @RequestParam String email
    ) {
        boolean exists = bizCardService.existsBizCard(userIdx, name, email);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "ok", exists));
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
                memoContent,
                null    // 태그는 여기서 안 넣음
        );
    }
}
