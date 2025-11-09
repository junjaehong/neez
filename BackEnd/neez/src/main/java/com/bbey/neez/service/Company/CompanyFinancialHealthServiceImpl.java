package com.bbey.neez.service.Company;

import com.bbey.neez.entity.Company;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class CompanyFinancialHealthServiceImpl implements CompanyFinancialHealthService {

    private final DartCorpCodeService corpCodeService;
    private final DartFinancialClient dartFinancialClient;

    public CompanyFinancialHealthServiceImpl(DartCorpCodeService corpCodeService,
                                             DartFinancialClient dartFinancialClient) {
        this.corpCodeService = corpCodeService;
        this.dartFinancialClient = dartFinancialClient;
    }

    @Override
    public BigDecimal evaluate(Company company) {
        try {
            String corpCode = corpCodeService.findCorpCodeByName(company.getName());
            if (corpCode == null) {
                // 못 찾으면 중간점
                return new BigDecimal("0.60");
            }

            // 최근 연도는 일단 하드코딩. 나중에 LocalDateTime.now().getYear() 써도 됨
            JsonNode node = dartFinancialClient.getFinancialStatements(corpCode, "2024", "11011");
            JsonNode list = node.path("list");
            if (list.isMissingNode()) {
                return new BigDecimal("0.60");
            }

            BigDecimal totalAssets = BigDecimal.ZERO;
            BigDecimal totalLiabilities = BigDecimal.ZERO;
            BigDecimal revenue = BigDecimal.ZERO;

            for (JsonNode item : list) {
                String accountName = item.path("account_nm").asText();
                String amountStr = item.path("thstrm_amount").asText("0").replaceAll(",", "");
                BigDecimal amount = new BigDecimal(amountStr.isEmpty() ? "0" : amountStr);

                if (accountName.contains("자산총계")) {
                    totalAssets = amount;
                } else if (accountName.contains("부채총계")) {
                    totalLiabilities = amount;
                } else if (accountName.contains("매출액") || accountName.contains("영업수익")) {
                    revenue = amount;
                }
            }

            BigDecimal debtScore = scoreByDebt(totalAssets, totalLiabilities);
            BigDecimal revenueScore = scoreByRevenue(revenue);

            return debtScore.multiply(new BigDecimal("0.6"))
                    .add(revenueScore.multiply(new BigDecimal("0.4")))
                    .setScale(2, RoundingMode.HALF_UP);

        } catch (Exception e) {
            return new BigDecimal("0.60");
        }
    }

    private BigDecimal scoreByDebt(BigDecimal assets, BigDecimal liabilities) {
        if (assets.compareTo(BigDecimal.ZERO) <= 0) {
            return new BigDecimal("0.50");
        }
        BigDecimal debtRatio = liabilities
                .divide(assets, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100")); // %
        // 100% 이하면 최고점
        if (debtRatio.compareTo(new BigDecimal("100")) <= 0) {
            return new BigDecimal("1.00");
        }
        // 200% 이상이면 최저점 0.30
        if (debtRatio.compareTo(new BigDecimal("200")) >= 0) {
            return new BigDecimal("0.30");
        }
        // 100~200 사이 선형
        BigDecimal diff = debtRatio.subtract(new BigDecimal("100")); // 0~100
        BigDecimal ratio = diff.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP); // 0~1
        BigDecimal penalty = ratio.multiply(new BigDecimal("0.70")); // 0~0.7
        return new BigDecimal("1.00").subtract(penalty).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal scoreByRevenue(BigDecimal revenue) {
        // 100억 이하 -> 0.4, 1,000억 이상 -> 1.0
        BigDecimal boundLow = new BigDecimal("10000000000");   // 100억
        BigDecimal boundHigh = new BigDecimal("100000000000"); // 1,000억

        if (revenue.compareTo(boundHigh) >= 0) {
            return new BigDecimal("1.00");
        }
        if (revenue.compareTo(boundLow) <= 0) {
            return new BigDecimal("0.40");
        }

        // 100억~1,000억 사이 선형
        BigDecimal range = boundHigh.subtract(boundLow);
        BigDecimal pos = revenue.subtract(boundLow);
        BigDecimal ratio = pos.divide(range, 4, RoundingMode.HALF_UP); // 0~1
        return new BigDecimal("0.40")
                .add(ratio.multiply(new BigDecimal("0.60")))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
