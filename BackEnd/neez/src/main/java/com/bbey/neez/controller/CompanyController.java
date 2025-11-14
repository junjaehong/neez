package com.bbey.neez.controller;

import com.bbey.neez.DTO.CompanyDto;
import com.bbey.neez.repository.CompanyRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyRepository companyRepository;

    public CompanyController(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyDto> getCompany(@PathVariable Long id) {
        return companyRepository.findById(id)
                .map(c -> ResponseEntity.ok(CompanyDto.from(c)))
                .orElse(ResponseEntity.notFound().build());
    }
}
