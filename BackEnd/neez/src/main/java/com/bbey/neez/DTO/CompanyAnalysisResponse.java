package com.bbey.neez.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompanyAnalysisResponse {

    private String companyName;
    private LocalDateTime analyzedAt;

    // 종합 점수
    private BigDecimal finalScore;

    // 세부 점수
    private Components components;

    // 어떤 소스를 실제로 썼는지
    private List<String> sources;

    // 실패한 부분 기록
    private List<String> warnings;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Components {
        private BigDecimal newsSentiment;
        private BigDecimal financialHealth;
        private BigDecimal esgProxy;
    }
}
