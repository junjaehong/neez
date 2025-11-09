package com.bbey.neez.controller;

import com.bbey.neez.DTO.ApiResponseDto;
import com.bbey.neez.DTO.CompanyScoreDto;
import com.bbey.neez.service.Company.CompanyAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "회사 종합 분석", description = "뉴스 감성 + 재무 건전성 + ESG 프록시를 종합해서 점수를 반환합니다.")
    @GetMapping("/analyze")
    public ResponseEntity<ApiResponseDto<CompanyScoreDto>> analyze(@RequestParam String name) {
        CompanyScoreDto dto = companyAnalysisService.evaluateCompany(name);
        return ResponseEntity.ok(new ApiResponseDto<>(true, "ok", dto));
    }
}
