package com.bbey.neez.repository;

import com.bbey.neez.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    // 회사명으로 찾기
    Optional<Company> findByName(String name);

    // 도메인으로도 식별 가능하게
    Optional<Company> findByDomain(String domain);
}
