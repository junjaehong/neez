package com.bbey.neez.service.Company;

import com.bbey.neez.entity.Company;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class CompanyFinancialHealthServiceImpl implements CompanyFinancialHealthService {

    @Override
    public BigDecimal evaluate(Company company) {
        // TODO: 여기서 나중에 DART/공시 붙이면 됨
        // 지금은 산업군/소스 같은 걸로 대충 기본값
        if (company.getIndustry() != null &&
            (company.getIndustry().contains("금융") || company.getIndustry().contains("은행"))) {
            return new BigDecimal("0.72");
        }
        return new BigDecimal("0.65");
    }
}
