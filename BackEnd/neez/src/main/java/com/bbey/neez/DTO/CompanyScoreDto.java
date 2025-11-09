package com.bbey.neez.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyScoreDto {
    private Long companyId;
    private String companyName;

    // 0~1 사이 점수들
    private BigDecimal newsSentiment;     // 뉴스 감성
    private BigDecimal financialHealth;   // 재무 건전성
    private BigDecimal esgProxy;          // ESG 프록시
    private BigDecimal finalScore;        // 종합 점수
}
