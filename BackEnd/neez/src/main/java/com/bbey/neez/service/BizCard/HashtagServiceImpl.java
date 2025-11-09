package com.bbey.neez.service.BizCard;

import com.bbey.neez.DTO.BizCardDto;
import com.bbey.neez.component.MemoStorage;
import com.bbey.neez.entity.*;
import com.bbey.neez.repository.BizCardRepository;
import com.bbey.neez.repository.CardHashTagRepository;
import com.bbey.neez.repository.HashTagRepository;
import com.bbey.neez.repository.CompanyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class HashtagServiceImpl implements HashtagService {

    private final BizCardRepository bizCardRepository;
    private final HashTagRepository hashTagRepository;
    private final CardHashTagRepository cardHashTagRepository;
    private final CompanyRepository companyRepository;
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

    @Override
    public void addTagToCard(Long cardId, String tagName) {
        BizCard card = bizCardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("BizCard not found: " + cardId));

        // ✅ 1번: 정규화
        String normalized = normalize(tagName);

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
        if (tagNames == null) return;
        for (String t : tagNames) {
            if (t == null || t.trim().isEmpty()) continue;
            addTagToCard(cardId, t);
        }
    }

    @Override
    public List<String> getTagsOfCard(Long cardId) {
        BizCard card = bizCardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("BizCard not found: " + cardId));

        List<CardHashTag> list = cardHashTagRepository.findByCard(card);
        List<String> result = new ArrayList<>();
        for (CardHashTag c : list) {
            result.add(c.getTag().getName());
        }
        return result;
    }

    @Override
    public Page<BizCardDto> getCardsByTags(List<String> tagNames, Pageable pageable) {
        // 1) 태그 정규화
        List<String> normalized = tagNames.stream()
                .filter(Objects::nonNull)
                .map(this::normalize)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

        if (normalized.isEmpty()) {
            return Page.empty(pageable);
        }

        // 2) 이 태그들을 전부 갖고 있는 카드 id 목록만 먼저 뽑음
        List<Long> cardIds = cardHashTagRepository.findCardIdsByAllTags(normalized, normalized.size());
        if (cardIds.isEmpty()) {
            return Page.empty(pageable);
        }

        // 3) 여기서부터는 페이징
        return bizCardRepository.findByIdxInAndIsDeletedFalse(cardIds, pageable)
                .map(card -> {
                    String companyName = null;
                    if (card.getCompanyIdx() != null) {
                        companyName = companyRepository.findById(card.getCompanyIdx())
                                .map(Company::getName)
                                .orElse(null);
                    }

                    String memoContent = "";
                    if (card.getMemo() != null && !card.getMemo().isEmpty()) {
                        try { memoContent = memoStorage.read(card.getMemo()); } catch (Exception ignored) {}
                    }

                    List<String> tagsOfCard = getTagsOfCard(card.getIdx());

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
                            tagsOfCard
                    );
                });
    }


    @Override
    public void removeTagFromCard(Long cardId, String tagName) {
        BizCard card = bizCardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("BizCard not found: " + cardId));

        String normalized = normalize(tagName);

        // ✅ 4번: 없으면 조용히 무시
        HashTag tag = hashTagRepository.findByName(normalized).orElse(null);
        if (tag == null) return;

        cardHashTagRepository.deleteByCardAndTag(card, tag);
    }

    @Override
    public List<HashTag> getTopTags(int limit) {
        // ORDER BY COUNT(...) DESC 는 쿼리 안에 already 있으니까 정렬 안 줘도 되지만
        // 혹시 모를 상황 대비해서 한 번 더 정렬 넣어도 됨.
        return hashTagRepository
                .findTopUsedTags(PageRequest.of(0, limit))
                .stream()
                .limit(limit)   // 혹시 더 와도 자르기
                .collect(Collectors.toList());
    }

    private String normalize(String tagName) {
        return tagName == null ? "" : tagName.trim().toLowerCase();
    }
}
