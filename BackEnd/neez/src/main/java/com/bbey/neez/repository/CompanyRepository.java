package com.bbey.neez.repository;

import com.bbey.neez.entity.Company;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long>{
    Optional<Company> findByName(String name);
}
