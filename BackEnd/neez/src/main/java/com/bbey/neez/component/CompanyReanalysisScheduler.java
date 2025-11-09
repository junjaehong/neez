package com.bbey.neez.component;

import com.bbey.neez.entity.Company;
import com.bbey.neez.repository.CompanyRepository;
import com.bbey.neez.service.Company.CompanyAnalysisService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CompanyReanalysisScheduler {

    private final CompanyRepository companyRepository;
    private final CompanyAnalysisService companyAnalysisService;

    public CompanyReanalysisScheduler(CompanyRepository companyRepository,
                                      CompanyAnalysisService companyAnalysisService) {
        this.companyRepository = companyRepository;
        this.companyAnalysisService = companyAnalysisService;
    }

    // 매일 새벽 3시
    @Scheduled(cron = "0 0 3 * * *")
    public void reanalyzeAll() {
        List<Company> companies = companyRepository.findAll();
        for (Company c : companies) {
            // 최신 연도/사업보고서 기준으로 다시 계산
            companyAnalysisService.evaluateCompany(c.getName(), "2024", "11011");
        }
    }
}
