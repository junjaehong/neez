package com.bbey.neez.service.Company;

import com.bbey.neez.component.DartAccountMapping;
import com.bbey.neez.entity.Company;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class CompanyFinancialHealthServiceImpl implements CompanyFinancialHealthService {

    private final DartCorpCodeService corpCodeService;
    private final DartFinancialClient dartFinancialClient;
    private final DartAccountMapping dartAccountMapping;

    public CompanyFinancialHealthServiceImpl(DartCorpCodeService corpCodeService,
                                             DartFinancialClient dartFinancialClient,
                                             DartAccountMapping dartAccountMapping) {
        this.corpCodeService = corpCodeService;
        this.dartFinancialClient = dartFinancialClient;
        this.dartAccountMapping = dartAccountMapping;
    }

    @Override
    public BigDecimal evaluate(Company company, String year, String reportCode) {
        try {
            String corpCode = corpCodeService.findCorpCodeByName(company.getName());
            if (corpCode == null) {
                return new BigDecimal("0.60");
            }

            JsonNode node = dartFinancialClient.getFinancialStatements(corpCode, year, reportCode);
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

                if (dartAccountMapping.matches(accountName, dartAccountMapping.totalAssetNames())) {
                    totalAssets = amount;
                } else if (dartAccountMapping.matches(accountName, dartAccountMapping.totalLiabilityNames())) {
                    totalLiabilities = amount;
                } else if (dartAccountMapping.matches(accountName, dartAccountMapping.revenueNames())) {
                    revenue = amount;
                }
            }

            BigDecimal debtScore = scoreByDebt(totalAssets, totalLiabilities);
            BigDecimal revenueScore = scoreByRevenue(revenue);

            return debtScore.multiply(new BigDecimal("0.6"))
                    .add(revenueScore.multiply(new BigDecimal("0.4")))
                    .setScale(2, RoundingMode.HALF_UP);

        } catch (Exception e) {
            // 로그만 남겨도 됨
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

        if (debtRatio.compareTo(new BigDecimal("100")) <= 0) {
            return new BigDecimal("1.00");
        }
        if (debtRatio.compareTo(new BigDecimal("200")) >= 0) {
            return new BigDecimal("0.30");
        }

        BigDecimal diff = debtRatio.subtract(new BigDecimal("100")); // 0~100
        BigDecimal ratio = diff.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP); // 0~1
        BigDecimal penalty = ratio.multiply(new BigDecimal("0.70"));
        return new BigDecimal("1.00").subtract(penalty).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal scoreByRevenue(BigDecimal revenue) {
        BigDecimal boundLow = new BigDecimal("10000000000");   // 100억
        BigDecimal boundHigh = new BigDecimal("100000000000"); // 1,000억

        if (revenue.compareTo(boundHigh) >= 0) {
            return new BigDecimal("1.00");
        }
        if (revenue.compareTo(boundLow) <= 0) {
            return new BigDecimal("0.40");
        }

        BigDecimal range = boundHigh.subtract(boundLow);
        BigDecimal pos = revenue.subtract(boundLow);
        BigDecimal ratio = pos.divide(range, 4, RoundingMode.HALF_UP);
        return new BigDecimal("0.40")
                .add(ratio.multiply(new BigDecimal("0.60")))
                .setScale(2, RoundingMode.HALF_UP);
    }
}
