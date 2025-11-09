package com.bbey.neez.service.Company;

import com.bbey.neez.DTO.CompanyEvaluationDto;
import com.bbey.neez.DTO.CompanyScoreDto;

public interface CompanyAnalysisService {

    // 전체 파이프라인 한 번에
    CompanyScoreDto evaluateCompany(String companyName);
}
