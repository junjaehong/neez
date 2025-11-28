package com.bbey.neez.repository;

import com.bbey.neez.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByName(String name);

    Optional<Company> findByBizNo(String bizNo);

    Optional<Company> findByCorpNo(String corpNo);

    Optional<Company> findFirstByNameAndAddress(String name, String address);

    // 이름 + 주소 + 도메인 + 홈페이지에 대해 부분검색
    @Query(
        "SELECT c " +
        "FROM Company c " +
        "WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
        "   OR LOWER(c.address) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
        "   OR LOWER(c.domain) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
        "   OR LOWER(c.homepage) LIKE LOWER(CONCAT('%', :keyword, '%'))"
    )
    List<Company> searchByKeyword(String keyword, Pageable pageable);
    
}
