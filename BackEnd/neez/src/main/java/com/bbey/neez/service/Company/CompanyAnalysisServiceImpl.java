package com.bbey.neez.service.Company;

import com.bbey.neez.DTO.CompanyEvaluationDto;
import com.bbey.neez.entity.Company;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CompanyAnalysisServiceImpl implements CompanyAnalysisService {

    private final CompanyIdentificationService identificationService;
    private final CompanyNewsSentimentService newsSentimentService;
    private final CompanyFinancialHealthService financialHealthService;
    private final CompanyEsgProxyService esgProxyService;

    public CompanyAnalysisServiceImpl(CompanyIdentificationService identificationService,
                                      CompanyNewsSentimentService newsSentimentService,
                                      CompanyFinancialHealthService financialHealthService,
                                      CompanyEsgProxyService esgProxyService) {
        this.identificationService = identificationService;
        this.newsSentimentService = newsSentimentService;
        this.financialHealthService = financialHealthService;
        this.esgProxyService = esgProxyService;
    }

    @Override
    public CompanyEvaluationDto evaluateCompany(String name, String domain) {
        // 1) 기업 식별/등록
        Company company = identificationService.identifyOrCreate(name, domain);

        // 2) 단계별 점수
        BigDecimal idScore   = identificationService.calcIdentificationConfidence(company);
        BigDecimal newsScore = newsSentimentService.analyzeNewsSentiment(company);
        BigDecimal finScore  = financialHealthService.evaluateFinancialHealth(company);
        BigDecimal esgScore  = esgProxyService.evaluateEsgProxy(company);

        // 3) 종합 점수 (단순 평균 → 나중에 가중치로 변경 가능)
        BigDecimal total = idScore
                .add(newsScore)
                .add(finScore)
                .add(esgScore)
                .divide(new BigDecimal("4"), 2, BigDecimal.ROUND_HALF_UP);

        return new CompanyEvaluationDto(
                company.getIdx(),
                company.getName(),
                idScore,
                newsScore,
                finScore,
                esgScore,
                total,
                company.getDetail()
        );
    }
}
