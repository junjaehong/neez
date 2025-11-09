package com.bbey.neez.repository;

import com.bbey.neez.entity.CardHashTag;
import com.bbey.neez.entity.BizCard;
import com.bbey.neez.entity.HashTag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface CardHashTagRepository extends JpaRepository<CardHashTag, Long> {

    // 카드에 달린 태그들
    List<CardHashTag> findByCard(BizCard card);

    // 특정 태그에 달린 카드들
    List<CardHashTag> findByTag(HashTag tag);

    Page<CardHashTag> findByTag(HashTag tag, Pageable pageable);

    // 중복 방지 체크
    boolean existsByCardAndTag(BizCard card, HashTag tag);

    // 카드에서 태그 하나 떼기
    void deleteByCardAndTag(BizCard card, HashTag tag);
}
