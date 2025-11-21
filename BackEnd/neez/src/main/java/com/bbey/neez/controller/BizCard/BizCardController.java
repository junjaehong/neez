package com.bbey.neez.controller.BizCard;

import com.bbey.neez.DTO.ApiResponseDto;
import com.bbey.neez.DTO.BizCardDto;
import com.bbey.neez.DTO.cardRequest.*;
import com.bbey.neez.entity.BizCard;
import com.bbey.neez.entity.BizCardSaveResult;
import com.bbey.neez.service.BizCard.BizCardService;
import com.bbey.neez.security.SecurityUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/bizcards")
@Tag(name = "BizCard CRUD API", description = "명함 수기 등록, 조회, 수정, 삭제, 복구, 검색")
@SecurityRequirement(name = "BearerAuth") // ✅ 모든 BizCard CRUD는 JWT 필요
public class BizCardController {

    private final BizCardService bizCardService;

    public BizCardController(BizCardService bizCardService) {
        this.bizCardService = bizCardService;
    }

    // (옵션) 디버그용 - 필요 없으면 삭제해도 됨
    @GetMapping("/me/test")
    public ApiResponseDto<Object> myBizCardTest() {
        Long userIdx = SecurityUtil.getCurrentUserIdx();
        return new ApiResponseDto<>(true, "현재 유저 idx: " + userIdx, null);
    }

    // 🔹 내 명함 목록 조회 (/me)
    @Operation(summary = "내 명함 목록 조회", description = "현재 로그인한 사용자의 명함을 페이지 단위로 조회한다.")
    @GetMapping("/me")
    public ApiResponseDto<Object> getMyBizCards(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<BizCardDto> res = bizCardService.getMyBizCards(pageable);
        return new ApiResponseDto<>(true, "내 명함 목록 조회 성공", res);
    }

    // 🔹 내 명함 수기 등록 (/me/manual)
    @Operation(summary = "내 명함 수기 등록", description = "현재 로그인한 사용자의 명함을 수기로 등록한다.")
    @PostMapping("/me/manual")
    public ResponseEntity<ApiResponseDto<BizCardDto>> createMyManual(@RequestBody BizCardManualRequest data) {
        try {
            Long userIdx = SecurityUtil.getCurrentUserIdx(); // 🔑 여기서만 유저 가져옴

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
            BizCardDto dto = toBizCardDto(result.getBizCard(), null, null);

            return ResponseEntity.ok(
                    new ApiResponseDto<>(true, result.isExisting() ? "already exists" : "ok", dto));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponseDto<>(false, e.getMessage(), null));
        }
    }

    // ✅ 단건 조회 (추후에 서비스에서 "내 명함인지" 체크 넣어도 됨)
    @Operation(summary = "명함 상세 조회", description = "명함 1건의 상세 정보를 조회한다.")
    @GetMapping("/{idx}")
    public ResponseEntity<ApiResponseDto<BizCardDto>> getBizCard(@PathVariable Long idx) {
        try {
            Map<String, Object> card = bizCardService.getBizCardDetail(idx);

            @SuppressWarnings("unchecked")
            List<String> tags = (List<String>) (card.get("hashtags") != null ? card.get("hashtags")
                    : new ArrayList<String>());

            BizCardDto dto = new BizCardDto(
                    (Long) card.get("idx"),
                    (Long) card.get("user_idx"),
                    (String) card.get("name"),
                    (String) card.get("card_company_name"),
                    (Long) card.get("company_idx"),
                    (String) card.get("department"),
                    (String) card.get("position"),
                    (String) card.get("email"),
                    (String) card.get("phone_number"),
                    (String) card.get("line_number"),
                    (String) card.get("fax_number"),
                    (String) card.get("address"),
                    (String) card.get("memo_content"),
                    tags);

            return ResponseEntity.ok(new ApiResponseDto<>(true, "ok", dto));
        } catch (Exception e) {
            return ResponseEntity.status(404)
                    .body(new ApiResponseDto<>(false, e.getMessage(), null));
        }
    }

    // 🔻 기존 /user/{userIdx}/page 는 제거하고 /me로 단일화했으니 주석/삭제

    // 🔹 내 명함 검색 (/me/search)
    @Operation(summary = "내 명함 검색", description = "현재 로그인한 사용자의 명함을 키워드로 검색한다.")
    @GetMapping("/me/search")
    public ResponseEntity<ApiResponseDto<Page<BizCardDto>>> searchMyBizCards(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageRequest pageable = PageRequest.of(page, size);
        Page<BizCardDto> result = bizCardService.searchMyBizCards(keyword, pageable);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "ok", result));
    }

    // 🔹 내 삭제된 명함 목록 (/me/deleted)
    @Operation(summary = "삭제된 내 명함 목록 조회", description = "현재 로그인한 사용자의 삭제된 명함 목록을 조회한다.")
    @GetMapping("/me/deleted")
    public ResponseEntity<ApiResponseDto<Page<BizCardDto>>> getMyDeletedBizCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageRequest pageable = PageRequest.of(page, size);
        Page<BizCardDto> result = bizCardService.getMyDeletedBizCards(pageable);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "ok", result));
    }

    // 🔹 내 명함 개수 (/me/count)
    @Operation(summary = "내 명함 개수 조회", description = "현재 로그인한 사용자의 명함 개수를 조회한다.")
    @GetMapping("/me/count")
    public ResponseEntity<ApiResponseDto<Long>> countMyBizCards() {
        long count = bizCardService.countMyBizCards();
        return ResponseEntity.ok(new ApiResponseDto<>(true, "ok", count));
    }

    // 🔹 내 명함 중복 여부 (/me/exists)
    @Operation(summary = "내 명함 중복 확인", description = "현재 로그인한 사용자의 명함 중에 동일 이름+이메일이 존재하는지 확인한다.")
    @GetMapping("/me/exists")
    public ResponseEntity<ApiResponseDto<Boolean>> existsMyBizCard(
            @RequestParam String name,
            @RequestParam String email) {

        boolean exists = bizCardService.existsMyBizCard(name, email);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "ok", exists));
    }

    // ✅ 수정 (여기서는 idx만 받고, "내 명함인지" 검증은 서비스 쪽에서 처리하는 것이 깔끔)
    @Operation(summary = "명함 정보 수정")
    @PutMapping("/{idx}")
    public ResponseEntity<ApiResponseDto<BizCardDto>> updateBizCard(
            @PathVariable Long idx,
            @RequestBody BizCardUpdateRequest body) {
        try {
            Map<String, String> map = new HashMap<>();
            if (body.getName() != null)
                map.put("name", body.getName());
            if (body.getCompany() != null)
                map.put("company", body.getCompany());
            if (body.getCompany_idx() != null)
                map.put("company_idx", body.getCompany_idx().toString());
            if (body.getDepartment() != null)
                map.put("department", body.getDepartment());
            if (body.getPosition() != null)
                map.put("position", body.getPosition());
            if (body.getEmail() != null)
                map.put("email", body.getEmail());
            if (body.getMobile() != null)
                map.put("mobile", body.getMobile());
            if (body.getTel() != null)
                map.put("tel", body.getTel());
            if (body.getFax() != null)
                map.put("fax", body.getFax());
            if (body.getAddress() != null)
                map.put("address", body.getAddress());

            boolean rematchCompany = Boolean.TRUE.equals(body.getRematchCompany());

            BizCard updated = bizCardService.updateBizCard(idx, map, rematchCompany);
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
    @PatchMapping("/{idx}/restore")
    public ResponseEntity<ApiResponseDto<Void>> restoreBizCard(@PathVariable Long idx) {
        try {
            bizCardService.restoreBizCard(idx);
            return ResponseEntity.ok(new ApiResponseDto<>(true, "restored", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponseDto<>(false, e.getMessage(), null));
        }
    }

    private BizCardDto toBizCardDto(BizCard card, String ignoredCompanyName, String memoContent) {
        if (card == null)
            return null;

        return new BizCardDto(
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
                memoContent,
                null);
    }
}
