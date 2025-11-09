package com.bbey.neez.service.Company;

import com.bbey.neez.component.NaverNewsClient;
import com.bbey.neez.component.OpenAiClient;
import com.bbey.neez.DTO.NaverNewsItem;
import com.bbey.neez.entity.Company;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

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
        // 1) 뉴스 여러 개 가져오기
        List<NaverNewsItem> items = naverNewsClient.searchNews(company.getName(), 5);

        if (items == null || items.isEmpty()) {
            // 뉴스 없으면 중립 점수
            return new BigDecimal("0.5");
        }

        // 2) 뉴스 목록을 하나의 문자열로 합치기
        //    제목 + 설명만 간단히 붙여서 보낼게
        String newsText = items.stream()
                .map(it -> "title: " + it.getTitle() + "\ndesc: " + it.getDescription())
                .collect(Collectors.joining("\n---\n"));

        // 3) OpenAI 호출해서 감성 점수 얻기 (0.0 ~ 1.0 이라고 가정)
        double score = openAiClient.analyzeSentiment(newsText);

        // 4) BigDecimal 로 변환해서 리턴
        return BigDecimal.valueOf(score);
    }
}
