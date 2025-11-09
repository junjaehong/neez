package com.bbey.neez.service.Company;

import com.bbey.neez.entity.Company;
import org.springframework.stereotype.Service;
import com.bbey.neez.component.NaverNewsClient;
import com.bbey.neez.component.OpenAiClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class CompanyNewsSentimentServiceImpl implements CompanyNewsSentimentService {

    private final NaverNewsClient naverNewsClient;
    private final OpenAiClient openAiClient;

    public CompanyNewsSentimentServiceImpl(NaverNewsClient naverNewsClient,
                                           OpenAiClient openAiClient) {
        this.naverNewsClient = naverNewsClient;
        this.openAiClient = openAiClient;
    }

    @Override
    public BigDecimal analyzeNewsSentiment(Company company) {
        // 회사 이름으로 최근 뉴스 가져오기
        List<Map<String, Object>> items = naverNewsClient.searchNews(company.getName(), 7);
        if (items.isEmpty()) {
            return new BigDecimal("0.5");
        }

        double sum = 0.0;
        int cnt = 0;
        for (Map<String, Object> item : items) {
            String title = (String) item.get("title");
            String desc = (String) item.get("description");
            String text = ((title != null) ? title : "") + " " + ((desc != null) ? desc : "");
            text = text.replaceAll("<.*?>", ""); // 태그 제거

            double score = openAiClient.analyzeSentiment(text);
            sum += score;
            cnt++;
        }

        double avg = (cnt == 0) ? 0.5 : sum / cnt;
        return BigDecimal
                .valueOf(avg)
                .setScale(2, BigDecimal.ROUND_HALF_UP);
    }
}
