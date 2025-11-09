package com.bbey.neez.component;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DartAccountMapping {

    // 자산총계로 인정할 이름들
    public List<String> totalAssetNames() {
        return Arrays.asList("자산총계", "자산 총계", "자산합계");
    }

    public List<String> totalLiabilityNames() {
        return Arrays.asList("부채총계", "부채 총계", "부채합계");
    }

    public List<String> revenueNames() {
        return Arrays.asList("매출액", "영업수익", "매출 총액");
    }

    public boolean matches(String accountName, List<String> candidates) {
        for (String c : candidates) {
            if (accountName != null && accountName.contains(c)) {
                return true;
            }
        }
        return false;
    }
}
