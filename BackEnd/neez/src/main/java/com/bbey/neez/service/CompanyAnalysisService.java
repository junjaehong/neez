package com.bbey.neez.service;

import com.bbey.neez.DTO.CompanyEvaluationDto;

public interface CompanyAnalysisService {

    // 전체 파이프라인 한 번에
    CompanyEvaluationDto evaluateCompany(String name, String domain);
}
