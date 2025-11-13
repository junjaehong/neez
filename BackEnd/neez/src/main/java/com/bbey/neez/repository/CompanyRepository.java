package com.bbey.neez.repository;

import com.bbey.neez.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByName(String name);

    Optional<Company> findByBizNo(String bizNo);

    Optional<Company> findByCorpNo(String corpNo);
}
