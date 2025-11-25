package com.bbey.neez.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class OpenAiChatClient {

  private static final String DEFAULT_BASE_URL = "https://generativelanguage.googleapis.com";
  private static final String DEFAULT_MODEL = "models/gemini-1.5-flash";
  private static final String FALLBACK_MESSAGE = "요약 서비스가 구성되지 않아 자동 요약을 제공할 수 없습니다.";
  private static final Logger log = LoggerFactory.getLogger(OpenAiChatClient.class);

  private final WebClient webClient;
  private final String model;
  private final double temperature;
  private final boolean enabled;

  public OpenAiChatClient(
      WebClient.Builder builder,
      @Value("${gemini.api-key:${openai.api-key:}}") String apiKey,
      @Value("${gemini.model:${openai.model:" + DEFAULT_MODEL + "}}") String model,
      @Value("${gemini.temperature:0.2}") double temperature,
      @Value("${gemini.base-url:" + DEFAULT_BASE_URL + "}") String baseUrl) {

    this.model = model;
    this.temperature = temperature;

    if (!StringUtils.hasText(apiKey)) {
      log.warn("Gemini/OpenAI API key not configured (gemini.api-key or openai.api-key). OpenAiChatClient disabled.");
      this.webClient = builder.baseUrl(baseUrl).build();
      this.enabled = false;
      return;
    }

    this.webClient = builder
        .baseUrl(baseUrl)
        .defaultHeader("x-goog-api-key", apiKey)
        .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        .build();

    this.enabled = true;
  }

  public String summarize(String transcript) {
    if (!enabled) {
      return FALLBACK_MESSAGE;
    }

    GeminiRequest request = GeminiRequest.of(model, temperature, transcript);

    GeminiResponse response = webClient.post()
        .uri(uriBuilder -> uriBuilder
            .path("/v1beta/")          // 변경: 변수 치환 대신 경로 조립
            .path(model)               // model에 포함된 '/'를 그대로 사용
            .path(":generateContent")
            .build())
        .bodyValue(request)
        .retrieve()
        .bodyToMono(GeminiResponse.class)
        .block();

    if (response == null) {
      throw new IllegalStateException("Gemini 응답이 없습니다.");
    }

    return response.firstText()
        .orElseThrow(() -> new IllegalStateException("Gemini 응답에서 요약을 찾을 수 없습니다"));
  }

  // === DTOs ===

  private static class GeminiRequest {
    private final List<Content> contents;
    private final GenerationConfig generationConfig;

    private GeminiRequest(List<Content> contents, GenerationConfig generationConfig) {
      this.contents = contents;
      this.generationConfig = generationConfig;
    }

    static GeminiRequest of(String model, double temperature, String transcript) {
      // 'system'은 유효하지 않음. Google GL API는 'model' 또는 'user'를 사용.
      Content system = Content.of("model", "You are an assistant that provides concise bullet summaries of meeting transcripts.");
      Content user = Content.of("user", transcript);
      GenerationConfig config = new GenerationConfig(temperature);

      return new GeminiRequest(Arrays.asList(system, user), config);
    }

    public List<Content> getContents() {
      return contents;
    }

    public GenerationConfig getGenerationConfig() {
      return generationConfig;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  private static class Content {
    private String role;
    private List<Part> parts;

    Content() {
      // for Jackson
    }

    private Content(String role, List<Part> parts) {
      this.role = role;
      this.parts = parts;
    }

    static Content of(String role, String text) {
      return new Content(role, Collections.singletonList(new Part(text)));
    }

    public String getRole() {
      return role;
    }

    public void setRole(String role) {
      this.role = role;
    }

    public List<Part> getParts() {
      return parts;
    }

    public void setParts(List<Part> parts) {
      this.parts = parts;
    }
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  private static class Part {
    private String text;

    Part() {
      // for Jackson
    }

    Part(String text) {
      this.text = text;
    }

    public String getText() {
      return text;
    }

    public void setText(String text) {
      this.text = text;
    }
  }

  private static class GenerationConfig {
    private final double temperature;

    GenerationConfig(double temperature) {
      this.temperature = temperature;
    }

    public double getTemperature() {
      return temperature;
    }
  }

  private static class GeminiResponse {
    private List<Candidate> candidates;

    Optional<String> firstText() {
      if (candidates == null || candidates.isEmpty()) {
        return Optional.empty();
      }
      return candidates.stream()
          .map(Candidate::firstText)
          .filter(Optional::isPresent)
          .map(Optional::get)
          .findFirst();
    }

    public List<Candidate> getCandidates() {
      return candidates;
    }

    public void setCandidates(List<Candidate> candidates) {
      this.candidates = candidates;
    }
  }

  private static class Candidate {
    private Content content;

    Optional<String> firstText() {
      if (content == null || content.getParts() == null || content.getParts().isEmpty()) {
        return Optional.empty();
      }
      return Optional.ofNullable(content.getParts().get(0).getText());
    }

    public Content getContent() {
      return content;
    }

    public void setContent(Content content) {
      this.content = content;
    }
  }
}
