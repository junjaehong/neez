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

    String src = normalizeForPapago(sourceLang, "auto");
    // 인지할 수 없는 코드라도 무조건 영어로 떨어지지 않도록 기본값을 auto로 변경
    String tgt = normalizeForPapago(targetLang, "auto");
    return translateInternal(text, src, tgt);
  }

  public Optional<String> translateToKoreanAuto(String text) {
    if (!enabled || !StringUtils.hasText(text)) {
      return Optional.empty();
    }

    String detected = normalizeForPapago(detectLanguage(text).orElse("auto"), "auto");
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

  /**
   * Papago가 지원하는 언어코드로 매핑 (ko,en,ja,zh-CN,zh-TW,vi,th,id,fr,es,ru,de,it,pt,auto)
   */
  private String normalizeForPapago(String lang, String defaultValue) {
    if (!StringUtils.hasText(lang)) {
      return defaultValue;
    }
    String val = lang.trim().toLowerCase().replace('_', '-');
    switch (val) {
      case "ko":
      case "ko-kr":
        return "ko";
      case "en":
      case "en-us":
      case "en-gb":
        return "en";
      case "ja":
      case "ja-jp":
        return "ja";
      case "zh-cn":
      case "zh-hans":
      case "cn":
        return "zh-CN";
      case "zh-tw":
      case "zh-hant":
      case "tw":
        return "zh-TW";
      case "es":
      case "es-es":
      case "es-mx":
        return "es";
      case "fr":
      case "fr-fr":
        return "fr";
      case "vi":
      case "vi-vn":
        return "vi";
      case "th":
        return "th";
      case "id":
      case "id-id":
        return "id";
      case "auto":
        return "auto";
      default:
        return defaultValue;
    }
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
