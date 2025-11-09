package com.bbey.neez.service;

import com.bbey.neez.entity.Company;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class CompanyFinancialHealthServiceImpl implements CompanyFinancialHealthService {

    @Override
    public BigDecimal evaluateFinancialHealth(Company company) {
        // TODO: 재무제표 연동 전까지는 기본값
        return new BigDecimal("0.8");
    }
}
