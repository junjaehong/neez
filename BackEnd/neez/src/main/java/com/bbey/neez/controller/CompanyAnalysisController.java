package com.bbey.neez.controller;

import com.bbey.neez.DTO.ApiResponseDto;
import com.bbey.neez.DTO.CompanyAnalysisResponse;
import com.bbey.neez.service.Company.CompanyAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies")
@Tag(name = "Company Analysis", description = "회사 신뢰도/분석 API")
public class CompanyAnalysisController {

    private final CompanyAnalysisService companyAnalysisService;

    public CompanyAnalysisController(CompanyAnalysisService companyAnalysisService) {
        this.companyAnalysisService = companyAnalysisService;
    }

    @Operation(
            summary = "회사 종합 분석",
            description = "뉴스 감성 + DART 재무 + ESG 프록시를 합산한 종합 점수를 반환합니다. year/reprtCode를 주면 특정 보고서 기준으로 분석합니다."
    )
    @GetMapping("/analyze")
    public ResponseEntity<ApiResponseDto<CompanyAnalysisResponse>> analyze(
            @RequestParam String name,
            @Parameter(description = "사업연도, 기본 2024") @RequestParam(required = false, defaultValue = "2024") String year,
            @Parameter(description = "보고서 코드(11011=사업, 11012=반기, 11013=1분기, 11014=3분기)", example = "11011")
            @RequestParam(required = false, defaultValue = "11011") String reportCode
    ) {
        CompanyAnalysisResponse dto = companyAnalysisService.evaluateCompany(name, year, reportCode);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "ok", dto));
    }
}
