package com.bbey.neez.controller;

import com.bbey.neez.DTO.ApiResponseDto;
import com.bbey.neez.DTO.CompanyEvaluationDto;
import com.bbey.neez.service.Company.CompanyAnalysisService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/companies")
@Tag(name = "Company Analysis", description = "회사 신뢰도 평가 및 정보 분석")
public class CompanyAnalysisController {

    private final CompanyAnalysisService companyAnalysisService;

    public CompanyAnalysisController(CompanyAnalysisService companyAnalysisService) {
        this.companyAnalysisService = companyAnalysisService;
    }

    @Operation(summary = "회사 평가 실행", description = "회사명(+도메인)을 기반으로 식별 → 감성 → 재무 → ESG → 종합 점수를 계산합니다.")
    @GetMapping("/evaluate")
    public ResponseEntity<ApiResponseDto<CompanyEvaluationDto>> evaluate(
            @RequestParam String name,
            @RequestParam(required = false) String domain
    ) {
        CompanyEvaluationDto dto = companyAnalysisService.evaluateCompany(name, domain);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "ok", dto));
    }
}
