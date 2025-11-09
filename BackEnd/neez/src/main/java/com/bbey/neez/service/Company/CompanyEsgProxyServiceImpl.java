package com.bbey.neez.service.Company;

import com.bbey.neez.entity.Company;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class CompanyEsgProxyServiceImpl implements CompanyEsgProxyService {

    @Override
    public BigDecimal evaluateEsgProxy(Company company) {
        // TODO: 산업별 기본 ESG 점수 매핑 가능
        return new BigDecimal("0.65");
    }
}
