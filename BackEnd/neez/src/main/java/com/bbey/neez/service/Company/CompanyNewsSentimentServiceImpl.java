package com.bbey.neez.service;

import com.bbey.neez.entity.Company;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class CompanyNewsSentimentServiceImpl implements CompanyNewsSentimentService {

    @Override
    public BigDecimal analyzeNewsSentiment(Company company) {
        // TODO: 실제로는 뉴스 크롤링/감성 분석 호출
        return new BigDecimal("0.7");
    }
}
