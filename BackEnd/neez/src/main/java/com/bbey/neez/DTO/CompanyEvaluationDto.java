package com.bbey.neez.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyEvaluationDto {

    private Long companyId;
    private String companyName;

    // 1) 기업 식별 신뢰도
    private BigDecimal identificationScore;

    // 2) 뉴스 감성 점수 (-1 ~ 1 혹은 0~1 스케일)
    private BigDecimal newsSentimentScore;

    // 3) 재무 건전성 (0~1)
    private BigDecimal financialHealthScore;

    // 4) ESG 프록시 (0~1)
    private BigDecimal esgProxyScore;

    // 5) 종합 점수 (0~1)
    private BigDecimal totalScore;

    // 회사 설명
    private String detail;
}
