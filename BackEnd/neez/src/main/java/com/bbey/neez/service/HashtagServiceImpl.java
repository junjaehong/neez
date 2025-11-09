package com.bbey.neez.service;

import com.bbey.neez.DTO.BizCardDto;
import com.bbey.neez.component.MemoStorage;
import com.bbey.neez.entity.*;
import com.bbey.neez.repository.BizCardRepository;
import com.bbey.neez.repository.CardHashTagRepository;
import com.bbey.neez.repository.HashTagRepository;
import com.bbey.neez.repository.CompanyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
    public Page<BizCardDto> getCardsByTag(String tagName, Pageable pageable) {
        String normalized = normalize(tagName);

        HashTag tag = hashTagRepository.findByName(normalized)
                .orElseThrow(() -> new RuntimeException("HashTag not found: " + normalized));

        Page<CardHashTag> page = cardHashTagRepository.findByTag(tag, pageable);

        return page.map(m -> {
            BizCard card = m.getCard();
            // 삭제된 건 걸러내고 싶으면 여기서 필터링
            if (card.isDeleted()) {
                return null; // 나중에 프론트에서 null 제거해도 되고, 여기서 throw해도 됨
            }

            String companyName = null;
            if (card.getCompanyIdx() != null) {
                companyName = companyRepository.findById(card.getCompanyIdx())
                        .map(Company::getName)
                        .orElse(null);
            }

            String memoContent = "";
            if (card.getMemo() != null && !card.getMemo().isEmpty()) {
                try {
                    memoContent = memoStorage.read(card.getMemo());
                } catch (IOException ignored) {}
            }

            // 태그 목록도 포함
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
        return hashTagRepository.findTopUsedTags(limit);
    }

    private String normalize(String tagName) {
        return tagName == null ? "" : tagName.trim().toLowerCase();
    }
}
