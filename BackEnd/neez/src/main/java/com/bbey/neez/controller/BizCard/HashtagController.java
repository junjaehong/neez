package com.bbey.neez.controller.BizCard;

import com.bbey.neez.DTO.ApiResponseDto;
import com.bbey.neez.DTO.BizCardDto;
import com.bbey.neez.entity.HashTag;
import com.bbey.neez.service.BizCard.HashtagService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@Tag(name = "Hashtag API", description = "명함 해시태그 부여/해제/조회")
@SecurityRequirement(name = "BearerAuth")
public class HashtagController {

    private final HashtagService hashtagService;

    public HashtagController(HashtagService hashtagService) {
        this.hashtagService = hashtagService;
    }

    @Operation(summary = "명함에 해시태그 부여")
    @PostMapping("/bizcards/{cardId}/hashtags")
    public ResponseEntity<ApiResponseDto<Void>> addTags(
            @PathVariable Long cardId,
            @RequestBody List<String> tags
    ) {
        hashtagService.addTagsToCard(cardId, tags);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "tags added", null));
    }

    @Operation(summary = "명함에 달린 해시태그 조회")
    @GetMapping("/bizcards/{cardId}/hashtags")
    public ResponseEntity<ApiResponseDto<List<String>>> getTags(@PathVariable Long cardId) {
        List<String> tags = hashtagService.getTagsOfCard(cardId);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "ok", tags));
    }

    @Operation(summary = "해시태그로 명함 검색")
    @GetMapping("/hashtags/search")
    public ResponseEntity<ApiResponseDto<Page<BizCardDto>>> searchByTags(
            @Parameter(description = "콤마(,)로 구분된 해시태그 목록", example = "고객사,vip")
            @RequestParam("tags") String tags,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<String> tagList = Arrays.stream(tags.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        PageRequest pageable = PageRequest.of(page, size);
        Page<BizCardDto> result = hashtagService.getCardsByTags(tagList, pageable);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "ok", result));
    }

    @Operation(summary = "명함에서 해시태그 제거")
    @DeleteMapping("/bizcards/{cardId}/hashtags/{tagName}")
    public ResponseEntity<ApiResponseDto<Void>> removeTag(
            @PathVariable Long cardId,
            @PathVariable String tagName
    ) {
        hashtagService.removeTagFromCard(cardId, tagName);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "tag removed", null));
    }

    @Operation(summary = "가장 많이 쓰이는 해시태그 TOP N")
    @GetMapping("/hashtags/top")
    public ResponseEntity<ApiResponseDto<List<HashTag>>> topTags(
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<HashTag> tags = hashtagService.getTopTags(limit);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "ok", tags));
    }
}
