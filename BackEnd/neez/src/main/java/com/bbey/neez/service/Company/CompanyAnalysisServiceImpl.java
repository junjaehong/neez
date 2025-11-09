package com.bbey.neez.service.Company;

import com.bbey.neez.DTO.CompanyScoreDto;
import com.bbey.neez.entity.Company;
import com.bbey.neez.repository.CompanyRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
public class CompanyAnalysisServiceImpl implements CompanyAnalysisService {

    private final CompanyIdentificationService identificationService;
    private final CompanyNewsSentimentService newsSentimentService;
    private final CompanyFinancialHealthService financialHealthService;
    private final CompanyEsgProxyService esgProxyService;
    private final CompanyRepository companyRepository;

    public CompanyAnalysisServiceImpl(CompanyIdentificationService identificationService,
                                      CompanyNewsSentimentService newsSentimentService,
                                      CompanyFinancialHealthService financialHealthService,
                                      CompanyEsgProxyService esgProxyService,
                                      CompanyRepository companyRepository) {
        this.identificationService = identificationService;
        this.newsSentimentService = newsSentimentService;
        this.financialHealthService = financialHealthService;
        this.esgProxyService = esgProxyService;
        this.companyRepository = companyRepository;
    }

    @Override
    public CompanyScoreDto evaluateCompany(String companyName) {
        // 1) 회사 찾기/만들기
        Company company = identificationService.findOrCreate(companyName);

        // 2) 뉴스 감성
        BigDecimal newsScore = newsSentimentService.analyzeNewsSentiment(company);  // 0~1

        // 3) 재무
        BigDecimal finScore = financialHealthService.evaluate(company);             // 0~1

        // 4) ESG
        BigDecimal esgScore = esgProxyService.evaluate(company);                    // 0~1

        // 5) 가중합 (원하면 바꿔)
        // 예: 뉴스 0.4, 재무 0.4, ESG 0.2
        BigDecimal finalScore =
                newsScore.multiply(new BigDecimal("0.4"))
                        .add(finScore.multiply(new BigDecimal("0.4")))
                        .add(esgScore.multiply(new BigDecimal("0.2")))
                        .setScale(2, RoundingMode.HALF_UP);

        // 6) company 테이블에도 일부 저장
        company.setConfidence(finalScore);
        company.setLast_refreshed_at(LocalDateTime.now());
        company.setUpdated_at(LocalDateTime.now());
        companyRepository.save(company);

        // 7) DTO 리턴
        return new CompanyScoreDto(
                company.getIdx(),
                company.getName(),
                newsScore,
                finScore,
                esgScore,
                finalScore
        );
    }
}
