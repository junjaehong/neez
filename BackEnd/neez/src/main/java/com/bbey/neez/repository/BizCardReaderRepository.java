package com.bbey.neez.repository;

import com.bbey.neez.entity.BizCard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BizCardReaderRepository extends JpaRepository<BizCard, Long> {
    // Spring Data JPA provides findById and save implementations
}