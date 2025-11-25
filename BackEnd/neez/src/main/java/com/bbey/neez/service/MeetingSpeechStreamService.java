package com.bbey.neez.service;

import com.bbey.neez.client.ClovaSpeechClient;
import com.bbey.neez.client.ClovaSpeechClient.ClovaResult;
import com.bbey.neez.client.ClovaSpeechClient.SpeakerSegment;
import com.bbey.neez.client.PapagoTranslationClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

@Service
public class MeetingSpeechStreamService {

  private final ClovaSpeechClient clovaClient;
  private final PapagoTranslationClient translationClient;
  private final String defaultSourceLanguage;

  /** (userIdx, meetingId) 복합키 기준 세션 저장 */
  private final ConcurrentMap<SessionKey, ConcurrentSkipListMap<Long, Segment>> sessions = new ConcurrentHashMap<>();

  /** 세션 식별자 */
  private static final class SessionKey {
    private final Long userIdx;
    private final Long meetingId;

    private SessionKey(Long userIdx, Long meetingId) {
      this.userIdx = userIdx;
      this.meetingId = meetingId;
    }

    public static SessionKey of(Long userIdx, Long meetingId) {
      return new SessionKey(userIdx, meetingId);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o)
        return true;
      if (!(o instanceof SessionKey))
        return false;
      SessionKey that = (SessionKey) o;
      return Objects.equals(userIdx, that.userIdx)
          && Objects.equals(meetingId, that.meetingId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(userIdx, meetingId);
    }
  }

  public MeetingSpeechStreamService(ClovaSpeechClient clovaClient,
      PapagoTranslationClient translationClient,
      @Value("${naver.clova.speech.language:ko-KR}") String sourceLanguage) {
    this.clovaClient = clovaClient;
    this.translationClient = translationClient;
    this.defaultSourceLanguage = normalizeLanguage(sourceLanguage);
  }

  /**
   * 회의 음성 조각(chunk) 처리
   */
  public Segment processChunk(Long userIdx, Long meetingId,
      Long chunkIndex, MultipartFile chunk,
      String targetLang, String sourceLang) throws Exception {

    if (chunk == null || chunk.isEmpty()) {
      throw new IllegalArgumentException("Audio chunk must not be empty.");
    }

    long index = (chunkIndex != null) ? chunkIndex : System.currentTimeMillis();
    String normalizedSource = normalizeLanguage(
        StringUtils.hasText(sourceLang) ? sourceLang : defaultSourceLanguage);
    String clovaLanguage = resolveClovaLanguage(sourceLang);

    ClovaResult result = StringUtils.hasText(clovaLanguage)
        ? clovaClient.recognize(chunk.getBytes(), clovaLanguage)
        : clovaClient.recognize(chunk.getBytes());

    String translated = translate(result.getText(), targetLang, normalizedSource);
    String normalizedTarget = StringUtils.hasText(targetLang)
        ? normalizeLanguage(targetLang)
        : "ko";

    Segment segment = new Segment(
        index,
        chunk.getSize(),
        result.getText(),
        Instant.now(),
        result.getSegments(),
        normalizedSource,
        normalizedTarget,
        translated);

    SessionKey key = SessionKey.of(userIdx, meetingId);
    sessions
        .computeIfAbsent(key, k -> new ConcurrentSkipListMap<>())
        .put(segment.getIndex(), segment);

    return segment;
  }

  /**
   * 원본 transcript (모든 segment text 이어붙인 것)
   */
  public String getTranscriptText(Long userIdx, Long meetingId) {
    SessionKey key = SessionKey.of(userIdx, meetingId);
    ConcurrentSkipListMap<Long, Segment> map = sessions.get(key);
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

  /**
   * segment 전체 목록
   */
  public List<Segment> getSegments(Long userIdx, Long meetingId) {
    SessionKey key = SessionKey.of(userIdx, meetingId);
    ConcurrentSkipListMap<Long, Segment> map = sessions.get(key);
    if (map == null || map.isEmpty()) {
      return Collections.emptyList();
    }
    return new ArrayList<>(map.values());
  }

  /**
   * 한국어 transcript (번역 포함)
   */
  public String getKoreanTranscript(Long userIdx, Long meetingId) {
    SessionKey key = SessionKey.of(userIdx, meetingId);
    ConcurrentSkipListMap<Long, Segment> map = sessions.get(key);
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

  /**
   * 세션 초기화
   */
  public void clear(Long userIdx, Long meetingId) {
    sessions.remove(SessionKey.of(userIdx, meetingId));
  }

  /* ===== 내부 유틸 ===== */

  private String translate(String text, String targetLang, String sourceLang) {
    if (!StringUtils.hasText(text)) {
      return null;
    }
    if (!StringUtils.hasText(targetLang)) {
      // targetLang 명시 안 된 경우: 자동 한국어 번역
      return translationClient.translateToKoreanAuto(text).orElse(null);
    }
    String normalizedTarget = normalizeLanguage(targetLang);
    return translationClient.translate(text, sourceLang, normalizedTarget).orElse(null);
  }

  private String ensureKoreanText(Segment segment) {
    if (segment == null) {
      return null;
    }
    if (StringUtils.hasText(segment.getTranslatedText())
        && isKorean(segment.getTargetLanguage())) {
      return segment.getTranslatedText();
    }
    if (!StringUtils.hasText(segment.getText())) {
      return null;
    }
    // 번역 실패하면 원문 그대로 반환
    return translationClient.translateToKoreanAuto(segment.getText())
        .orElse(segment.getText());
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
    // 필요하면 언어코드 매핑 추가
    return trimmed;
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

  /**
   * STT/번역 결과 한 조각
   */
  public static class Segment {
    private final long index;
    private final long bytes;
    private final String text;
    private final Instant receivedAt;
    private final List<SpeakerSegment> speakerSegments;
    private final String sourceLanguage;
    private final String targetLanguage;
    private final String translatedText;

    public Segment(long index,
        long bytes,
        String text,
        Instant receivedAt,
        List<SpeakerSegment> speakerSegments,
        String sourceLanguage,
        String targetLanguage,
        String translatedText) {
      this.index = index;
      this.bytes = bytes;
      this.text = text;
      this.receivedAt = receivedAt;
      this.speakerSegments = speakerSegments != null
          ? speakerSegments
          : Collections.emptyList();
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
}
