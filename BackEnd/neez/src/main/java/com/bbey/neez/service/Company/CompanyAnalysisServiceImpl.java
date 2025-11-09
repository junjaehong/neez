package com.bbey.neez.service.Company;

import com.bbey.neez.DTO.CompanyAnalysisResponse;
import com.bbey.neez.entity.Company;
import com.bbey.neez.repository.CompanyRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    public CompanyAnalysisResponse evaluateCompany(String companyName, String year, String reportCode) {
        // 1. 회사 식별 or 생성
        Company company = identificationService.findOrCreate(companyName);

        List<String> warnings = new ArrayList<>();
        List<String> sources  = new ArrayList<>();

        // 2. 뉴스 감성 (double -> BigDecimal)
        BigDecimal newsScore = newsSentimentService.analyzeNewsSentiment(company);

        sources.add("NAVER_NEWS");
        sources.add("OPENAI");

        // 3. 재무 점수 (이미 BigDecimal이라고 가정)
        BigDecimal finScore = financialHealthService.evaluate(company, year, reportCode);
        sources.add("DART");

        // 4. ESG 점수 (이미 BigDecimal이라고 가정)
        BigDecimal esgScore = esgProxyService.evaluate(company);
        sources.add("ESG_PROXY");

        // 5. 종합 점수 = 0.4 * 뉴스 + 0.4 * 재무 + 0.2 * ESG
        BigDecimal finalScore = newsScore.multiply(new BigDecimal("0.4"))
                .add(finScore.multiply(new BigDecimal("0.4")))
                .add(esgScore.multiply(new BigDecimal("0.2")));

        // 6. 회사 테이블에 저장
        company.setConfidence(finalScore);
        company.setLast_refreshed_at(LocalDateTime.now());
        company.setUpdated_at(LocalDateTime.now());
        companyRepository.save(company);

        // 7. 응답 DTO 구성
        CompanyAnalysisResponse.Components components =
                new CompanyAnalysisResponse.Components(newsScore, finScore, esgScore);

        return new CompanyAnalysisResponse(
                company.getName(),
                LocalDateTime.now(),
                finalScore,
                components,
                sources,
                warnings
        );
    }
}
