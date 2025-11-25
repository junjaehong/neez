package com.bbey.neez.client;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

/**
 * Minimal client for Naver Papago NMT API.
 */
@Component
public class PapagoTranslationClient {

  private static final Logger log = LoggerFactory.getLogger(PapagoTranslationClient.class);

  private final WebClient webClient;
  private final boolean enabled;

  public PapagoTranslationClient(WebClient.Builder builder,
                                 @Value("${naver.papago.x-ncp-apigw-api-key-id:}") String apiKeyId,
                                 @Value("${naver.papago.x-ncp-apigw-api-key:}") String apiKey,
                                 @Value("${naver.papago.base-url:https://papago.apigw.ntruss.com}") String baseUrl) {

    if (!StringUtils.hasText(apiKeyId) || !StringUtils.hasText(apiKey)) {
      log.warn("Papago API key not configured (naver.papago.api-key-id/api-key). Translation disabled.");
      this.webClient = builder.baseUrl(baseUrl).build();
      this.enabled = false;
      return;
    }

    this.webClient = builder
        .baseUrl(baseUrl)
        .defaultHeader("X-NCP-APIGW-API-KEY-ID", apiKeyId)
        .defaultHeader("X-NCP-APIGW-API-KEY", apiKey)
        .build();
    this.enabled = true;
  }

  public Optional<String> translate(String text, String sourceLang, String targetLang) {
    if (!enabled || !StringUtils.hasText(text) || !StringUtils.hasText(targetLang)) {
      return Optional.empty();
    }

    return translateInternal(text,
        normalizeLanguage(sourceLang, "auto"),
        normalizeLanguage(targetLang, "en"));
  }

  public Optional<String> translateToKoreanAuto(String text) {
    if (!enabled || !StringUtils.hasText(text)) {
      return Optional.empty();
    }

    String detected = detectLanguage(text).orElse("auto");
    return translateInternal(text, detected, "ko");
  }

  private Optional<String> translateInternal(String text, String source, String target) {
    try {
      PapagoResponse response = webClient.post()
          .uri("/nmt/v1/translation")
          .contentType(MediaType.APPLICATION_FORM_URLENCODED)
          .body(BodyInserters
              .fromFormData("source", source)
              .with("target", target)
              .with("text", text))
          .retrieve()
          .bodyToMono(PapagoResponse.class)
          .block();

      if (response == null
          || response.getMessage() == null
          || response.getMessage().getResult() == null) {
        return Optional.empty();
      }
      return Optional.ofNullable(response.getMessage().getResult().getTranslatedText());
    } catch (WebClientResponseException e) {
      log.error("Papago translation HTTP {} error: {}", e.getRawStatusCode(),
          e.getResponseBodyAsString(), e);
    } catch (Exception e) {
      log.error("Papago translation failed", e);
    }

    return Optional.empty();
  }

  private Optional<String> detectLanguage(String text) {
    if (!StringUtils.hasText(text)) {
      return Optional.empty();
    }
    try {
      DetectResponse response = webClient.post()
          .uri("/langs/v1/dect")
          .contentType(MediaType.APPLICATION_FORM_URLENCODED)
          .body(BodyInserters.fromFormData("query", text))
          .retrieve()
          .bodyToMono(DetectResponse.class)
          .block();
      if (response == null) {
        return Optional.empty();
      }
      return Optional.ofNullable(response.getLangCode());
    } catch (WebClientResponseException e) {
      log.error("Papago language detection HTTP {} error: {}", e.getRawStatusCode(),
          e.getResponseBodyAsString(), e);
    } catch (Exception e) {
      log.error("Papago language detection failed", e);
    }
    return Optional.empty();
  }

  private String normalizeLanguage(String lang, String defaultValue) {
    if (!StringUtils.hasText(lang)) {
      return defaultValue;
    }
    String normalized = lang.trim().toLowerCase().replace('_', '-');
    int dash = normalized.indexOf('-');
    if (dash > 0) {
      normalized = normalized.substring(0, dash);
    }
    return normalized;
  }

  // --- DTOs for Papago response ---

  private static class PapagoResponse {
    private Message message;

    public Message getMessage() {
      return message;
    }

    public void setMessage(Message message) {
      this.message = message;
    }
  }

  private static class Message {
    private Result result;

    public Result getResult() {
      return result;
    }

    public void setResult(Result result) {
      this.result = result;
    }
  }

  private static class Result {
    private String translatedText;
    private String srcLangType;
    private String tarLangType;

    public String getTranslatedText() {
      return translatedText;
    }

    public void setTranslatedText(String translatedText) {
      this.translatedText = translatedText;
    }

    public String getSrcLangType() {
      return srcLangType;
    }

    public void setSrcLangType(String srcLangType) {
      this.srcLangType = srcLangType;
    }

    public String getTarLangType() {
      return tarLangType;
    }

    public void setTarLangType(String tarLangType) {
      this.tarLangType = tarLangType;
    }
  }

  private static class DetectResponse {
    private String langCode;

    public String getLangCode() {
      return langCode;
    }

    public void setLangCode(String langCode) {
      this.langCode = langCode;
    }
  }
}
