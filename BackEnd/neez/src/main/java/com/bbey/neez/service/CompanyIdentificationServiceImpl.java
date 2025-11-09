package com.bbey.neez.service;

import com.bbey.neez.entity.Company;
import com.bbey.neez.repository.CompanyRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class CompanyIdentificationServiceImpl implements CompanyIdentificationService {

    private final CompanyRepository companyRepository;

    public CompanyIdentificationServiceImpl(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public Company identifyOrCreate(String name, String domain) {
        // 1) 이름으로 먼저 찾기
        return companyRepository.findByName(name)
                .orElseGet(() -> {
                    Company c = new Company();
                    c.setName(name);
                    c.setDomain(domain);
                    c.setSource("manual");
                    c.setConfidence(new BigDecimal("0.6")); // 초기값
                    c.setCreated_at(LocalDateTime.now());
                    c.setUpdated_at(LocalDateTime.now());
                    return companyRepository.save(c);
                });
    }

    @Override
    public BigDecimal calcIdentificationConfidence(Company company) {
        // 일단은 도메인이 있으면 더 높게, 없으면 낮게
        if (company.getDomain() != null && !company.getDomain().isEmpty()) {
            return new BigDecimal("0.9");
        }
        return company.getConfidence() != null ? company.getConfidence() : new BigDecimal("0.6");
    }
}

