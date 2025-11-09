package com.bbey.neez.service;

import com.bbey.neez.entity.Company;
import java.math.BigDecimal;

public interface CompanyEsgProxyService {
    BigDecimal evaluateEsgProxy(Company company);
}
