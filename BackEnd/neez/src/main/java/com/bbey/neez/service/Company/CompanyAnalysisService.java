package com.bbey.neez.service.Company;

import com.bbey.neez.DTO.CompanyAnalysisResponse;

public interface CompanyAnalysisService {
    CompanyAnalysisResponse evaluateCompany(String companyName, String year, String reportCode);
}
