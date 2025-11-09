package com.bbey.neez.service.Company;

import com.bbey.neez.entity.Company;

import java.math.BigDecimal;

public interface CompanyIdentificationService {
    Company identifyOrCreate(String name, String domain);
    BigDecimal calcIdentificationConfidence(Company company);
}