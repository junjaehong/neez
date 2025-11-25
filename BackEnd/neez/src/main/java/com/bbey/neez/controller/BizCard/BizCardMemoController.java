package com.bbey.neez.controller.BizCard;

import com.bbey.neez.DTO.ApiResponseDto;
import com.bbey.neez.DTO.MemoDto;
import com.bbey.neez.entity.BizCard;
import com.bbey.neez.service.BizCard.BizCardMemoService;
import com.bbey.neez.DTO.cardRequest.*;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bizcards")
@Tag(name = "BizCard Memo API", description = "명함 메모 조회/수정 전용")
@SecurityRequirement(name = "BearerAuth")
public class BizCardMemoController {

    private final BizCardMemoService bizCardMemoService;

    public BizCardMemoController(BizCardMemoService bizCardMemoService) {
        this.bizCardMemoService = bizCardMemoService;
    }

    @Operation(summary = "명함 메모 조회")
    @GetMapping("/{id}/memo")
    public ResponseEntity<ApiResponseDto<MemoDto>> getMemo(@PathVariable Long id) {
        try {
            String memoContent = bizCardMemoService.getBizCardMemoContent(id);
            MemoDto dto = new MemoDto(id, memoContent, "card-" + id + ".txt");
            return ResponseEntity.ok(new ApiResponseDto<>(true, "ok", dto));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponseDto<>(false, e.getMessage(), null));
        }
    }

    @Operation(summary = "명함 메모 수정")
    @PatchMapping("/{id}/memo")
    public ResponseEntity<ApiResponseDto<MemoDto>> updateBizCardMemo(
            @PathVariable Long id,
            @RequestBody BizCardMemoUpdateRequest body
    ) {
        if (body.getMemo() == null) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponseDto<>(false, "memo is required", null));
        }

        BizCard updated = bizCardMemoService.updateBizCardMemo(id, body.getMemo());
        MemoDto dto = new MemoDto(id, body.getMemo(), updated.getMemo());
        return ResponseEntity.ok(new ApiResponseDto<>(true, "memo updated", dto));
    }
}
