package com.bbey.neez.controller;

import com.bbey.neez.DTO.ApiResponseDto;
import com.bbey.neez.DTO.CompanyDto;
import com.bbey.neez.repository.CompanyRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies")
@Tag(name = "Company API", description = "명함과 연결된 회사 정보 조회 API")
public class CompanyController {

    private final CompanyRepository companyRepository;

    public CompanyController(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Operation(
            summary = "회사 상세 조회",
            description = "companies.idx 기준으로 회사 상세 정보를 조회한다. " +
                            "명함 DTO의 companyIdx와 매핑되는 값이다."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<CompanyDto>> getCompany(
            @Parameter(description = "companies.idx", example = "3")
            @PathVariable Long id
    ) {
        return companyRepository.findById(id)
                .map(c -> {
                    CompanyDto dto = CompanyDto.from(c);
                    return ResponseEntity.ok(new ApiResponseDto<>(true, "ok", dto));
                })
                .orElse(ResponseEntity.status(404)
                        .body(new ApiResponseDto<>(false, "Company not found: " + id, null)));
    }
}
