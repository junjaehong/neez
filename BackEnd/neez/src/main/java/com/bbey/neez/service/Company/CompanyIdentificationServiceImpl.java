package com.bbey.neez.service.Company;

import com.bbey.neez.entity.Company;
import com.bbey.neez.repository.CompanyRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CompanyIdentificationServiceImpl implements CompanyIdentificationService {

    private final CompanyRepository companyRepository;

    public CompanyIdentificationServiceImpl(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Override
    public Company findOrCreate(String name) {
        return companyRepository.findByName(name)
                .orElseGet(() -> {
                    Company c = new Company();
                    c.setName(name);
                    c.setCreated_at(LocalDateTime.now());
                    c.setUpdated_at(LocalDateTime.now());
                    return companyRepository.save(c);
                });
    }
}
