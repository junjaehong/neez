package com.bbey.neez.service.BizCard;

import com.bbey.neez.DTO.BizCardDto;
import com.bbey.neez.component.MemoStorage;
import com.bbey.neez.entity.BizCard;
import com.bbey.neez.entity.CardHashTag;
import com.bbey.neez.entity.HashTag;
import com.bbey.neez.exception.AccessDeniedBizException;
import com.bbey.neez.exception.ResourceNotFoundException;
import com.bbey.neez.repository.BizCardRepository;
import com.bbey.neez.repository.CardHashTagRepository;
import com.bbey.neez.repository.HashTagRepository;
import com.bbey.neez.repository.CompanyRepository;
import com.bbey.neez.security.SecurityUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class HashtagServiceImpl implements HashtagService {

    private final BizCardRepository bizCardRepository;
    private final HashTagRepository hashTagRepository;
    private final CardHashTagRepository cardHashTagRepository;
    private final CompanyRepository companyRepository; // 현재는 사용 안 하지만 시그니처 유지
    private final MemoStorage memoStorage;

    public HashtagServiceImpl(BizCardRepository bizCardRepository,
            HashTagRepository hashTagRepository,
            CardHashTagRepository cardHashTagRepository,
            CompanyRepository companyRepository,
            MemoStorage memoStorage) {
        this.bizCardRepository = bizCardRepository;
        this.hashTagRepository = hashTagRepository;
        this.cardHashTagRepository = cardHashTagRepository;
        this.companyRepository = companyRepository;
        this.memoStorage = memoStorage;
    }

    /**
     * 현재 로그인한 사용자가 명함 소유자인지 검증
     */
    private void verifyOwnership(BizCard card) {
        Long currentUserIdx = SecurityUtil.getCurrentUserIdx();
        if (currentUserIdx == null || card == null || card.getUserIdx() != currentUserIdx) {
            throw new AccessDeniedBizException("해당 명함에 대한 해시태그 수정 권한이 없습니다.");
        }
    }

    @Override
    public void addTagToCard(Long cardId, String tagName) {
        BizCard card = bizCardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("BizCard not found: " + cardId));

        // 🔒 소유자 검증
        verifyOwnership(card);

        String normalized = normalize(tagName);
        if (normalized.isEmpty()) {
            return;
        }

        HashTag tag = hashTagRepository.findByName(normalized)
                .orElseGet(() -> {
                    HashTag t = new HashTag();
                    t.setName(normalized);
                    return hashTagRepository.save(t);
                });

        if (cardHashTagRepository.existsByCardAndTag(card, tag)) {
            return;
        }

        CardHashTag cht = new CardHashTag();
        cht.setCard(card);
        cht.setTag(tag);
        cardHashTagRepository.save(cht);
    }

    @Override
    public void addTagsToCard(Long cardId, List<String> tagNames) {
        if (tagNames == null)
            return;
        for (String t : tagNames) {
            if (t == null || t.trim().isEmpty())
                continue;
            addTagToCard(cardId, t);
        }
    }

    @Override
    public List<String> getTagsOfCard(Long cardId) {
        BizCard card = bizCardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("BizCard not found: " + cardId));

        // 🔒 소유자 검증
        verifyOwnership(card);

        List<CardHashTag> list = cardHashTagRepository.findByCard(card);
        List<String> result = new ArrayList<>();
        for (CardHashTag c : list) {
            result.add(c.getTag().getName());
        }
        return result;
    }

    @Override
    public Page<BizCardDto> getCardsByTags(List<String> tagNames, Pageable pageable) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        // 태그 이름 정규화
        List<String> normalized = tagNames.stream()
                .filter(Objects::nonNull)
                .map(this::normalize)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        if (normalized.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        // ✅ 모든 태그를 가진 카드 id 목록 조회 (이미 있는 native query 활용)
        List<Long> cardIds = cardHashTagRepository.findCardIdsByAllTags(normalized, normalized.size());
        if (cardIds == null || cardIds.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        // ✅ 명함 조회 (소프트 삭제 제외)
        Page<BizCard> cardPage = bizCardRepository.findByIdxInAndIsDeletedFalse(cardIds, pageable);

        // ✅ BizCard -> BizCardDto 변환 (BizCardServiceImpl.toDto 와 동일한 형태)
        return cardPage.map(card -> {
            String memoContent = "";
            if (card.getMemo() != null && !card.getMemo().isEmpty()) {
                try {
                    memoContent = memoStorage.read(card.getMemo());
                } catch (IOException ignored) {
                }
            }

            // 카드에 달린 태그 목록 조회
            List<CardHashTag> tagsOfCard = cardHashTagRepository.findByCard(card);
            List<String> hashtags = tagsOfCard.stream()
                    .map(ch -> ch.getTag().getName())
                    .collect(Collectors.toList());

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
                    hashtags);
        });
    }

    @Override
    public void removeTagFromCard(Long cardId, String tagName) {
        BizCard card = bizCardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("BizCard not found: " + cardId));

        // 🔒 소유자 검증
        verifyOwnership(card);

        String normalized = normalize(tagName);
        if (normalized.isEmpty()) {
            return;
        }

        HashTag tag = hashTagRepository.findByName(normalized)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found: " + normalized));

        cardHashTagRepository.deleteByCardAndTag(card, tag);
    }

    @Override
    public List<HashTag> getTopTags(int limit) {
        return hashTagRepository
                .findTopUsedTags(PageRequest.of(0, limit))
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    private String normalize(String tagName) {
        return tagName == null ? "" : tagName.trim().toLowerCase();
    }
}
