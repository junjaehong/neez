package com.bbey.neez.service;

import com.bbey.neez.client.ClovaSpeechClient;
import com.bbey.neez.client.ClovaSpeechClient.ClovaResult;
import com.bbey.neez.client.ClovaSpeechClient.SpeakerSegment;
import com.bbey.neez.client.PapagoTranslationClient;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class MeetingSpeechStreamService {

  private final ClovaSpeechClient clovaClient;
  private final PapagoTranslationClient translationClient;
  private final String defaultSourceLanguage;
  private final ConcurrentMap<Long, ConcurrentSkipListMap<Long, Segment>> sessions =
      new ConcurrentHashMap<>();

  public MeetingSpeechStreamService(ClovaSpeechClient clovaClient,
                                    PapagoTranslationClient translationClient,
                                    @Value("${naver.clova.speech.language:ko-KR}") String sourceLanguage) {
    this.clovaClient = clovaClient;
    this.translationClient = translationClient;
    this.defaultSourceLanguage = normalizeLanguage(sourceLanguage);
  }

  public Segment processChunk(Long meetingId, Long chunkIndex, MultipartFile chunk,
                              String targetLang, String sourceLang) throws Exception {
    if (chunk == null || chunk.isEmpty()) {
      throw new IllegalArgumentException("Audio chunk must not be empty.");
    }

    long index = chunkIndex != null ? chunkIndex.longValue() : System.currentTimeMillis();
    String normalizedSource = normalizeLanguage(
        StringUtils.hasText(sourceLang) ? sourceLang : defaultSourceLanguage);
    String clovaLanguage = resolveClovaLanguage(sourceLang);
    ClovaResult result = StringUtils.hasText(clovaLanguage)
        ? clovaClient.recognize(chunk.getBytes(), clovaLanguage)
        : clovaClient.recognize(chunk.getBytes());
    String translated = translate(result.getText(), targetLang, normalizedSource);
    String normalizedTarget = StringUtils.hasText(targetLang) ? normalizeLanguage(targetLang) : "ko";

    Segment segment = new Segment(
        index,
        chunk.getSize(),
        result.getText(),
        Instant.now(),
        result.getSegments(),
        normalizedSource,
        normalizedTarget,
        translated);
    sessions
        .computeIfAbsent(meetingId, key -> new ConcurrentSkipListMap<>())
        .put(segment.getIndex(), segment);
    return segment;
  }

  public String getTranscriptText(Long meetingId) {
    ConcurrentSkipListMap<Long, Segment> map = sessions.get(meetingId);
    if (map == null || map.isEmpty()) {
      return "";
    }

    StringBuilder sb = new StringBuilder();
    for (Segment segment : map.values()) {
      if (StringUtils.hasText(segment.getText())) {
        if (sb.length() > 0) {
          sb.append(' ');
        }
        sb.append(segment.getText().trim());
      }
    }
    return sb.toString();
  }

  public List<Segment> getSegments(Long meetingId) {
    ConcurrentSkipListMap<Long, Segment> map = sessions.get(meetingId);
    if (map == null || map.isEmpty()) {
      return Collections.emptyList();
    }
    return new ArrayList<>(map.values());
  }

  public void clear(Long meetingId) {
    sessions.remove(meetingId);
  }

  public static class Segment {
    private final long index;
    private final long bytes;
    private final String text;
    private final Instant receivedAt;
    private final List<SpeakerSegment> speakerSegments;
    private final String sourceLanguage;
    private final String targetLanguage;
    private final String translatedText;

    Segment(long index, long bytes, String text, Instant receivedAt,
            List<SpeakerSegment> speakerSegments,
            String sourceLanguage,
            String targetLanguage,
            String translatedText) {
      this.index = index;
      this.bytes = bytes;
      this.text = text;
      this.receivedAt = receivedAt;
      this.speakerSegments = speakerSegments != null ? speakerSegments : Collections.emptyList();
      this.sourceLanguage = sourceLanguage;
      this.targetLanguage = targetLanguage;
      this.translatedText = translatedText;
    }

    public long getIndex() {
      return index;
    }

    public long getBytes() {
      return bytes;
    }

    public String getText() {
      return text;
    }

    public Instant getReceivedAt() {
      return receivedAt;
    }

    public List<SpeakerSegment> getSpeakerSegments() {
      return speakerSegments;
    }

    public String getSourceLanguage() {
      return sourceLanguage;
    }

    public String getTargetLanguage() {
      return targetLanguage;
    }

    public String getTranslatedText() {
      return translatedText;
    }
  }

  private String translate(String text, String targetLang, String sourceLang) {
    if (!StringUtils.hasText(text)) {
      return null;
    }
    if (!StringUtils.hasText(targetLang)) {
      return translationClient.translateToKoreanAuto(text).orElse(null);
    }
    String normalizedTarget = normalizeLanguage(targetLang);
    return translationClient.translate(text, sourceLang, normalizedTarget).orElse(null);
  }

  public String getKoreanTranscript(Long meetingId) {
    ConcurrentSkipListMap<Long, Segment> map = sessions.get(meetingId);
    if (map == null || map.isEmpty()) {
      return "";
    }

    StringBuilder sb = new StringBuilder();
    for (Segment segment : map.values()) {
      String text = ensureKoreanText(segment);
      if (StringUtils.hasText(text)) {
        if (sb.length() > 0) {
          sb.append(' ');
        }
        sb.append(text.trim());
      }
    }
    return sb.toString();
  }

  private String ensureKoreanText(Segment segment) {
    if (segment == null) {
      return null;
    }
    if (StringUtils.hasText(segment.getTranslatedText()) && isKorean(segment.getTargetLanguage())) {
      return segment.getTranslatedText();
    }
    if (!StringUtils.hasText(segment.getText())) {
      return null;
    }
    return translationClient.translateToKoreanAuto(segment.getText()).orElse(segment.getText());
  }

  private boolean isKorean(String lang) {
    if (!StringUtils.hasText(lang)) {
      return false;
    }
    return "ko".equals(normalizeLanguage(lang));
  }

  private String resolveClovaLanguage(String lang) {
    if (!StringUtils.hasText(lang)) {
      return null;
    }
    String trimmed = lang.trim();
    if (trimmed.contains("-")) {
      return trimmed;
    }
    switch (trimmed.toLowerCase()) {
      case "ko":
        return "ko-KR";
      case "en":
        return "en-US";
      case "ja":
        return "ja-JP";
      case "zh":
        return "zh-CN";
      default:
        return trimmed;
    }
  }

  private String normalizeLanguage(String lang) {
    if (!StringUtils.hasText(lang)) {
      return "ko";
    }
    String normalized = lang.trim().toLowerCase().replace('_', '-');
    int idx = normalized.indexOf('-');
    if (idx > 0) {
      normalized = normalized.substring(0, idx);
    }
    return normalized;
  }
}
