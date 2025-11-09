package com.bbey.neez.service;

import com.bbey.neez.DTO.BizCardDto;
import com.bbey.neez.entity.HashTag;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface HashtagService {

    // 카드에 태그 1개 붙이기 (없으면 생성)
    void addTagToCard(Long cardId, String tagName);

    // 카드에 여러 개 붙이기
    void addTagsToCard(Long cardId, List<String> tagNames);

    // 카드에 달린 태그 이름들
    List<String> getTagsOfCard(Long cardId);
    
    // 태그로 카드들 찾기 (페이징)
    Page<BizCardDto> getCardsByTags(List<String> tagNames, Pageable pageable);

    // 카드에서 태그 떼기
    void removeTagFromCard(Long cardId, String tagName);

    // 인기 태그 목록
    List<HashTag> getTopTags(int limit);
}
