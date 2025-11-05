package com.bbey.neez.repository;

import com.bbey.neez.entity.BizCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BizCardRepository extends JpaRepository<BizCard, Long> {

    // 이름 + 이메일만으로 중복 체크 (언더스코어 안 씀)
    Optional<BizCard> findByNameAndEmail(String name, String email);
}
