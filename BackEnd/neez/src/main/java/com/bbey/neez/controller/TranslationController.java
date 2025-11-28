package com.bbey.neez.controller;

import com.bbey.neez.client.PapagoTranslationClient;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/translate")
@Tag(name = "Translation API")  
@SecurityRequirement(name = "BearerAuth")
public class TranslationController {

  private final PapagoTranslationClient translationClient;

  public TranslationController(PapagoTranslationClient translationClient) {
    this.translationClient = translationClient;
  }

  @PostMapping
  public ResponseEntity<Map<String, Object>> translate(@RequestBody TranslateRequest request) {
    if (request == null || !StringUtils.hasText(request.getText()) || !StringUtils.hasText(request.getTargetLang())) {
      return ResponseEntity.badRequest().body(error("BAD_REQUEST", "text와 targetLang는 필수입니다."));
    }

    Optional<String> translated = translationClient.translate(
        request.getText(),
        request.getSourceLang(),
        request.getTargetLang()
    );

    Map<String, Object> payload = new HashMap<>();
    payload.put("text", request.getText());
    // Papago 비활성/오류 시에도 원문을 반환해 프런트가 끊기지 않도록 함
    payload.put("translatedText", translated.orElse(request.getText()));
    payload.put("sourceLang", request.getSourceLang());
    payload.put("targetLang", request.getTargetLang());
    return ResponseEntity.ok(payload);
  }

  private Map<String, Object> error(String code, String message) {
    Map<String, Object> m = new HashMap<>();
    m.put("error", code);
    m.put("message", message);
    return m;
  }

  public static class TranslateRequest {
    private String text;
    private String sourceLang;
    private String targetLang;

    public String getText() {
      return text;
    }

    public void setText(String text) {
      this.text = text;
    }

    public String getSourceLang() {
      return sourceLang;
    }

    public void setSourceLang(String sourceLang) {
      this.sourceLang = sourceLang;
    }

    public String getTargetLang() {
      return targetLang;
    }

    public void setTargetLang(String targetLang) {
      this.targetLang = targetLang;
    }
  }
}
