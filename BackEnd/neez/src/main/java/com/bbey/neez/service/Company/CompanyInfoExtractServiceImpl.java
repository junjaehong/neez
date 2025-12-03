package com.bbey.neez.service.company;

import com.bbey.neez.entity.Company;
import com.bbey.neez.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CompanyInfoExtractServiceImpl implements CompanyInfoExtractService {

    private final CompanyRepository companyRepository;

    @Override
    public Optional<Company> findExistingCompany(String name, String address) {
        if (name == null) {
            return Optional.empty();
        }

        String trimmedName = name.trim();
        if (trimmedName.isEmpty()) {
            return Optional.empty();
        }

        String trimmedAddress = (address != null ? address.trim() : null);

        // 1순위: 이름 + 주소 완전 일치
        if (trimmedAddress != null && !trimmedAddress.isEmpty()) {
            Optional<Company> exact =
                    companyRepository.findFirstByNameAndAddress(trimmedName, trimmedAddress);
            if (exact.isPresent()) {
                return exact;
            }
        }

        // 2순위: 이름만 동일
        return companyRepository.findByName(trimmedName);
    }
}
