package com.bbey.neez.service.Company;

import com.bbey.neez.entity.Company;

import java.util.Optional;

public interface CompanyInfoExtractService {

    /**
     * 회사명 + 주소 기반으로
     *  - BizNo 상호 검색
     *  - 금융위 기업개요조회
     *  - 매칭 점수 계산
     *  - companies 테이블에 저장 (이미 있으면 재사용)
     */
    Optional<Company> extractAndSave(String companyName, String address);

    /**
     * 외부 API는 사용하지 않고,
     * DB 기준으로 회사명을 매칭하고 없으면 생성하는 가벼운 메서드
     * 1) name + address 일치 검색
     * 2) name 만으로 검색
     * 3) 그래도 없으면 새 Company 생성
     */
    Optional<Company> matchOrCreateCompany(String companyName, String address);
}
