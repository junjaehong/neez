package com.bbey.neez.controller;

import com.bbey.neez.DTO.ApiResponseDto;
import com.bbey.neez.DTO.CompanyDto;
import com.bbey.neez.repository.CompanyRepository;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies")
@Tag(name = "Company API", description = "회사 정보 조회 API")
@SecurityRequirement(name = "BearerAuth")
public class CompanyController {

    private final CompanyRepository companyRepository;

    public CompanyController(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Operation(
            summary = "회사 단건 조회",
            description = "companies 테이블의 idx를 기준으로 회사 상세 정보를 조회한다."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<CompanyDto>> getCompany(@PathVariable Long id) {
        return companyRepository.findById(id)
                .map(c -> {
                    CompanyDto dto = CompanyDto.from(c);
                    return ResponseEntity.ok(
                            new ApiResponseDto<>(true, "ok", dto)
                    );
                })
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(new ApiResponseDto<>(false, "Company not found: " + id, null))
                );
    }
}
