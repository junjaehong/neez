package com.bbey.neez.service.company;

import com.bbey.neez.entity.Company;

import java.util.Optional;

public interface CompanyInfoExtractService {

    /**
     * DB에 이미 존재하는 회사만 검색한다.
     * - 1순위: 이름 + 주소 완전 일치
     * - 2순위: 이름만 일치
     * 찾지 못하면 Optional.empty()
     */
    Optional<Company> findExistingCompany(String name, String address);

}
