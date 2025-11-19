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
     *
     * @param companyName 명함에서 추출한 회사명
     * @param address     명함에서 추출한 주소
     */
    Optional<Company> extractAndSave(String companyName, String address);
}
