package com.bbey.neez.controller.BizCard;

import com.bbey.neez.DTO.ApiResponseDto;
import com.bbey.neez.DTO.BizCardDto;
import com.bbey.neez.DTO.request.BizCardManualRequest;
import com.bbey.neez.DTO.request.BizCardUpdateRequest;
import com.bbey.neez.entity.BizCard;
import com.bbey.neez.entity.BizCardSaveResult;
import com.bbey.neez.service.BizCard.BizCardService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/bizcards")
@Tag(name = "BizCard CRUD API", description = "명함 수기 등록 / OCR 등록 이후 조회·수정·삭제 / 회사 자동 매칭 포함")
public class BizCardController {

    private final BizCardService bizCardService;

    public BizCardController(BizCardService bizCardService) {
        this.bizCardService = bizCardService;
    }

    @Operation(summary = "명함 수기 등록", description = "사용자가 직접 입력한 명함 정보를 저장한다.\n" +
            "요청의 company(명함 회사명) + address를 기반으로 회사 자동 매칭을 시도하고,\n" +
            "companies 테이블에 회사 정보를 생성/갱신한 뒤,\n" +
            "bizcards.company_idx로 연결한다.")

    @PostMapping("/manual")
    public ResponseEntity<ApiResponseDto<BizCardDto>> createManual(
            @RequestBody BizCardManualRequest data) {
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
            BizCardDto dto = toBizCardDto(result.getBizCard(), null, null);

            return ResponseEntity.ok(
                    new ApiResponseDto<>(true, result.isExisting() ? "already exists" : "ok", dto));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponseDto<>(false, e.getMessage(), null));
        }
    }

    @Operation(summary = "명함 상세 조회", description = "명함 1건의 상세 정보를 조회한다.\n" +
            "응답에는 명함 원문 회사명(cardCompanyName)과 연결된 회사 IDX(companyIdx)가 모두 포함된다.\n" +
            "회사 상세 정보가 필요하면 /api/companies/{companyIdx}를 추가로 호출하면 된다.")

    @GetMapping("/{idx}")
    public ResponseEntity<ApiResponseDto<BizCardDto>> getBizCard(
            @Parameter(description = "명함 IDX", example = "10") @PathVariable Long idx) {
        try {
            Map<String, Object> card = bizCardService.getBizCardDetail(idx);

            @SuppressWarnings("unchecked")
            List<String> tags = (List<String>) (card.get("hashtags") != null
                    ? card.get("hashtags")
                    : new ArrayList<>());

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

    @Operation(summary = "사용자 명함 목록 조회", description = "특정 사용자의 명함 목록을 페이징 조회한다.")
    @GetMapping("/user/{userIdx}/page")
    public ResponseEntity<ApiResponseDto<Page<BizCardDto>>> getBizCardsPage(
            @Parameter(description = "사용자 IDX", example = "1") @PathVariable Long userIdx,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<BizCardDto> result = bizCardService.getBizCardsByUserIdx(userIdx, pageable);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "ok", result));
    }

    @Operation(summary = "명함 정보 수정", description = "명함의 기본 정보를 수정한다.\n" +
            "company_idx를 함께 보내면 명함과 연결된 회사(companies.idx)를 직접 변경할 수 있다.\n" +
            "명함 원문 회사명을 수정하고 재매칭하고 싶다면 별도 API로 확장 가능.")
    @PutMapping("/{idx}")
    public ResponseEntity<ApiResponseDto<BizCardDto>> updateBizCard(
            @Parameter(description = "명함 IDX", example = "10") @PathVariable Long idx,
            @RequestBody BizCardUpdateRequest body) {
        try {
            Map<String, String> map = new HashMap<>();
            if (body.getName() != null)
                map.put("name", body.getName());
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

            BizCard updated = bizCardService.updateBizCard(idx, map);
            BizCardDto dto = toBizCardDto(updated, null, null);
            return ResponseEntity.ok(new ApiResponseDto<>(true, "updated", dto));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponseDto<>(false, e.getMessage(), null));
        }
    }

    @Operation(summary = "명함 삭제", description = "명함을 논리 삭제(is_deleted = true) 처리한다.")
    @DeleteMapping("/{idx}")
    public ResponseEntity<ApiResponseDto<Void>> deleteBizCard(
            @Parameter(description = "명함 IDX", example = "10") @PathVariable Long idx) {
        try {
            bizCardService.deleteBizCard(idx);
            return ResponseEntity.ok(new ApiResponseDto<>(true, "deleted", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponseDto<>(false, e.getMessage(), null));
        }
    }

    @Operation(summary = "명함 복구", description = "논리 삭제된 명함을 복구한다.")
    @PatchMapping("/{id}/restore")
    public ResponseEntity<ApiResponseDto<Void>> restoreBizCard(
            @Parameter(description = "명함 IDX", example = "10") @PathVariable Long id) {
        try {
            bizCardService.restoreBizCard(id);
            return ResponseEntity.ok(new ApiResponseDto<>(true, "restored", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponseDto<>(false, e.getMessage(), null));
        }
    }

    @Operation(summary = "명함 검색", description = "사용자의 명함들 중에서 키워드로 검색한다.")
    @GetMapping("/user/{userIdx}/search")
    public ResponseEntity<ApiResponseDto<Page<BizCardDto>>> search(
            @Parameter(description = "사용자 IDX", example = "1") @PathVariable Long userIdx,
            @Parameter(description = "검색 키워드(이름/회사/이메일 등 포함)", example = "삼성") @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<BizCardDto> result = bizCardService.searchBizCards(userIdx, keyword, pageable);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "ok", result));
    }

    @Operation(summary = "삭제된 명함 목록 조회", description = "is_deleted = true 인 명함 목록을 페이징 조회한다.")
    @GetMapping("/user/{userIdx}/deleted")
    public ResponseEntity<ApiResponseDto<Page<BizCardDto>>> getDeletedBizCards(
            @Parameter(description = "사용자 IDX", example = "1") @PathVariable Long userIdx,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageRequest pageable = PageRequest.of(page, size);
        Page<BizCardDto> result = bizCardService.getDeletedBizCardsByUserIdx(userIdx, pageable);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "ok", result));
    }

    @Operation(summary = "명함 개수 조회", description = "사용자의 삭제되지 않은 명함 개수를 조회한다.")
    @GetMapping("/user/{userIdx}/count")
    public ResponseEntity<ApiResponseDto<Long>> countBizCards(
            @Parameter(description = "사용자 IDX", example = "1") @PathVariable Long userIdx) {
        long count = bizCardService.countBizCardsByUser(userIdx);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "ok", count));
    }

    @Operation(summary = "명함 중복 확인", description = "같은 사용자에 대해 이름+이메일이 같은 명함이 이미 존재하는지 확인한다.")
    @GetMapping("/user/{userIdx}/exists")
    public ResponseEntity<ApiResponseDto<Boolean>> existsBizCard(
            @Parameter(description = "사용자 IDX", example = "1") @PathVariable Long userIdx,
            @Parameter(description = "이름", example = "홍길동") @RequestParam String name,
            @Parameter(description = "이메일", example = "hong@gildong.com") @RequestParam String email) {
        boolean exists = bizCardService.existsBizCard(userIdx, name, email);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "ok", exists));
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
