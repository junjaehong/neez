package com.bbey.neez.service.Company;

import com.bbey.neez.entity.Company;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class CompanyEsgProxyServiceImpl implements CompanyEsgProxyService {

    @Override
    public BigDecimal evaluate(Company company) {
        // TODO: 나중에 ESG 뉴스 비중, 업종별 ESG 테이블로 교체
        if (company.getSource() != null && company.getSource().equalsIgnoreCase("krx")) {
            return new BigDecimal("0.68");
        }
        return new BigDecimal("0.6");
    }
}
