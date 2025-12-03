package com.bbey.neez.repository;

import com.bbey.neez.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    // 회사 자동 매칭용
    Optional<Company> findFirstByNameAndAddress(String name, String address);

    Optional<Company> findByName(String name);

    // 검색 API용
    Optional<Company> findByBizNo(String bizNo);

    Optional<Company> findByCorpNo(String corpNo);

    // 키워드 검색 (Java 8/11 호환)
    @Query("SELECT c " +
            "FROM Company c " +
            "WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(c.address) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(c.homepage) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(c.industry) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Company> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
