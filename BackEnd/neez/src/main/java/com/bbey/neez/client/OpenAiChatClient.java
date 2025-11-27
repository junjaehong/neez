package com.bbey.neez.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Collections;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@Slf4j
@Component
public class OpenAiChatClient {

    private static final String FALLBACK_MESSAGE = "- 요약 기능이 비활성화되었습니다.";

    private final WebClient webClient;
    private final String model;
    private final double temperature;
    private final boolean enabled;

    public OpenAiChatClient(
            @Value("${gemini.api-key}") String apiKey,
            @Value("${gemini.model}") String model,
            @Value("${gemini.temperature:0.2}") double temperature,
            @Value("${gemini.base-url}") String baseUrl) {
        this.model = model;
        this.temperature = temperature;
        this.enabled = StringUtils.hasText(apiKey);

        this.webClient = WebClient.builder()
                .baseUrl(baseUrl + "/v1beta") // https://generativelanguage.googleapis.com/v1beta
                .defaultHeader("x-goog-api-key", apiKey)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * 회의 내용 요약
     */
    public String summarize(String transcript) {
        if (!enabled)
            return FALLBACK_MESSAGE;
        if (!StringUtils.hasText(transcript))
            return "- 회의 내용이 비어 있습니다.";

        try {
            GeminiRequest request = GeminiRequest.fromTranscript(transcript, temperature);

            GeminiResponse response = webClient.post()
                    .uri("/models/{model}:generateContent", model)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(GeminiResponse.class)
                    .block();

            if (response == null || response.candidates == null || response.candidates.isEmpty())
                return "- 요약 결과가 비어 있습니다.";

            GeminiResponse.Candidate candidate = response.candidates.get(0);
            if (candidate == null || candidate.content == null)
                return "- 요약 결과가 비어 있습니다.";

            return candidate.content.extractTextOnly();

        } catch (WebClientResponseException e) {
            log.error("Gemini 요약 호출 실패 (HTTP {}): {}", e.getRawStatusCode(), e.getResponseBodyAsString());
            throw e;
        }
    }

    // -------------------------------------------------------
    // Gemini 요청/응답 구조
    // -------------------------------------------------------

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GeminiRequest {
        public List<Content> contents;
        public GenerationConfig generationConfig;

        public GeminiRequest() {
        } // 🔹 Jackson용 기본 생성자

        public GeminiRequest(List<Content> contents, GenerationConfig generationConfig) {
            this.contents = contents;
            this.generationConfig = generationConfig;
        }

        public static GeminiRequest fromTranscript(String transcript, double temperature) {

            String prompt = "너는 회의/통화 내용을 요약하는 전문가이다.\n" +
                    "아래 규칙을 반드시 지켜서 요약을 작성하라.\n" +
                    "\n" +
                    "1. 반드시 한국어로 작성한다.\n" +
                    "2. 출력은 '-' 로 시작하는 bullet list만 작성한다.\n" +
                    "3. 회의가 짧으면 1~2개의 bullet, 길면 주요 논의/결정/액션 위주로 여러 bullet 작성.\n" +
                    "4. '회의 내용이 짧다', '요약이 어렵다', '맥락이 부족하다' 등의 설명은 절대 쓰지 않는다.\n" +
                    "5. bullet 외의 문장, 제목, 마크다운(## 등), 결론 등은 절대 작성하지 않는다.\n" +
                    "\n" +
                    "다음은 요약할 회의 전문이다:\n";

            String fullText = prompt + transcript;

            Content userMsg = Content.user(fullText);
            GenerationConfig config = new GenerationConfig(temperature, 512);

            return new GeminiRequest(Collections.singletonList(userMsg), config);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Content {
        public String role; // "user"
        public List<Part> parts; // [{ text: "..." }]

        public Content() {
        } // 🔹 Jackson용 기본 생성자

        public Content(String role, List<Part> parts) {
            this.role = role;
            this.parts = parts;
        }

        public static Content user(String text) {
            return new Content("user", Collections.singletonList(new Part(text)));
        }

        public String extractTextOnly() {
            if (parts == null || parts.isEmpty())
                return null;

            StringBuilder sb = new StringBuilder();
            for (Part p : parts) {
                if (p.text != null)
                    sb.append(p.text);
            }
            return sb.toString().trim();
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Part {
        public String text;

        public Part() {
        } // 🔹 Jackson용 기본 생성자

        public Part(String text) {
            this.text = text;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GenerationConfig {
        public Double temperature;
        public Integer maxOutputTokens;

        public GenerationConfig() {
        } // 🔹 Jackson용 기본 생성자

        public GenerationConfig(Double temperature, Integer maxOutputTokens) {
            this.temperature = temperature;
            this.maxOutputTokens = maxOutputTokens;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class GeminiResponse {
        public List<Candidate> candidates;

        public GeminiResponse() {
        } // 🔹 Jackson용 기본 생성자

        @JsonIgnoreProperties(ignoreUnknown = true)
        private static class Candidate {
            public Content content;

            public Candidate() {
            } // 🔹 Jackson용 기본 생성자
        }
    }

}
