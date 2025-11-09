package com.bbey.neez.service.Company;

import com.bbey.neez.entity.Company;

import java.math.BigDecimal;

public interface CompanyFinancialHealthService {
    BigDecimal evaluate(Company company, String year, String reportCode);
}
