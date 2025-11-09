package com.bbey.neez.component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class OpenAiClient {

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.model:gpt-4o-mini}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 뉴스 한 건을 넣으면 0.0 ~ 1.0 사이 감성 점수를 돌려준다.
     */
    public double analyzeSentiment(String text) {
        String url = "https://api.openai.com/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        // 모델한테 숫자만 뱉으라고 강하게 시킨다.
        String userPrompt =
                "다음 한국어 뉴스 문장의 감성(긍정도)을 0에서 1 사이의 소수로만 답해.\n" +
                "0은 매우 부정, 0.5는 중립, 1은 매우 긍정이야.\n" +
                "숫자만 답해. 다른 말 하지 마.\n\n" +
                "문장: " + text;

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", new Object[]{
                new HashMap<String, String>() {{
                    put("role", "user");
                    put("content", userPrompt);
                }}
        });
        body.put("temperature", 0);   // 일관된 숫자 위해 0
        try {
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            JsonNode root = objectMapper.readTree(resp.getBody());
            String content = root.path("choices").get(0).path("message").path("content").asText().trim();
            double val = Double.parseDouble(content);
            if (val < 0) val = 0;
            if (val > 1) val = 1;
            return val;
        } catch (HttpClientErrorException.TooManyRequests e) {
            // 여기서만 잡아주면 지금 같은 429에서 안 터짐
            System.out.println("OpenAiClient.analyzeSentiment rate limit exceeded: " + e.getMessage());
            return 0.5; // 중립
        } catch (Exception e) {
            System.out.println("OpenAiClient.analyzeSentiment error: " + e.getMessage());
            return 0.5;
        }
    }
}
