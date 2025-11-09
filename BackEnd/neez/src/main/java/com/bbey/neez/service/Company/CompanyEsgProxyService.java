package com.bbey.neez.service.Company;

import com.bbey.neez.entity.Company;

import java.math.BigDecimal;

public interface CompanyEsgProxyService {
    BigDecimal evaluate(Company company);
}
