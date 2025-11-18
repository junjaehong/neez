package com.bbey.neez.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class NaverCsrSttClient {

  private static final String FALLBACK_MESSAGE = "Transcription unavailable.";

  private final WebClient webClient;
  private final String language;

  public NaverCsrSttClient(
      @Value("${naver.cloud.stt.client-id}") String clientId,
      @Value("${naver.cloud.stt.client-secret}") String clientSecret,
      @Value("${naver.cloud.stt.language:Kor}") String language,
      @Value("${naver.cloud.stt.base-url:https://naveropenapi.apigw.ntruss.com}") String baseUrl) {

    this.language = language;

    this.webClient = WebClient.builder()
        .baseUrl(baseUrl)
        .defaultHeader("X-NCP-APIGW-API-KEY-ID", clientId)
        .defaultHeader("X-NCP-APIGW-API-KEY", clientSecret)
        .build();
  }

  public String recognize(byte[] data) {
    if (data == null || data.length == 0) {
      throw new IllegalArgumentException("Audio payload must not be empty.");
    }

    return webClient.post()
        .uri(builder -> builder.path("/recog/v1/stt").queryParam("lang", language).build())
        .contentType(MediaType.APPLICATION_OCTET_STREAM)
        .bodyValue(data)
        .retrieve()
        .onStatus(HttpStatus::isError, response ->
            response.bodyToMono(String.class)
                .defaultIfEmpty("N/A")
                .flatMap(msg -> Mono.error(new RuntimeException(
                    String.format("CSR request failed: %s - %s", response.statusCode(), msg)
                )))
        )
        .bodyToMono(CsrResponse.class)
        .map(resp -> resp != null && resp.getText() != null ? resp.getText() : FALLBACK_MESSAGE)
        .block();
  }

  public static class CsrResponse {
    private String text;

    public String getText() {
      return text;
    }

    public void setText(String text) {
      this.text = text;
    }
  }
}