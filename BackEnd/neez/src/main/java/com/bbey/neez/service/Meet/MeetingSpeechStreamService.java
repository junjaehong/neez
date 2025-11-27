package com.bbey.neez.service.Meet;

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
  private final MeetingSttService meetingSttService;
  private final String defaultSourceLanguage;

  /** (userIdx, meetingId) 복합키 기준 세션 저장 */
  private final ConcurrentMap<SessionKey, ConcurrentSkipListMap<Long, Segment>> sessions =
      new ConcurrentHashMap<>();

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
      if (this == o) return true;
      if (!(o instanceof SessionKey)) return false;
      SessionKey that = (SessionKey) o;
      return Objects.equals(userIdx, that.userIdx)
          && Objects.equals(meetingId, that.meetingId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(userIdx, meetingId);
    }
  }

  public MeetingSpeechStreamService(
      ClovaSpeechClient clovaClient,
      PapagoTranslationClient translationClient,
      MeetingSttService meetingSttService,
      @Value("${naver.clova.speech.language:ko-KR}") String sourceLanguage) {

    this.clovaClient = clovaClient;
    this.translationClient = translationClient;
    this.meetingSttService = meetingSttService;
    // 내부 표현용 기본 소스 언어(normalize)
    this.defaultSourceLanguage = normalizeLanguage(sourceLanguage);
  }

  /**
   * 회의 음성 조각(chunk) 처리
   */
  public Segment processChunk(
      Long userIdx,
      Long meetingId,
      Long index,
      MultipartFile chunk,
      String targetLang,
      String sourceLang) throws Exception {

    // 1) 소스/타깃 언어 정리
    String normalizedSource =
        normalizeLanguage(StringUtils.hasText(sourceLang) ? sourceLang : defaultSourceLanguage);
    String normalizedTarget = normalizeLanguage(targetLang);

    // 2) Clova STT 호출 언어 결정 (Clova가 요구하는 포맷으로 매핑)
    String clovaLanguage = resolveClovaLanguage(sourceLang);
    ClovaResult result = StringUtils.hasText(clovaLanguage)
        ? clovaClient.recognize(chunk.getBytes(), clovaLanguage)
        : clovaClient.recognize(chunk.getBytes());

    // 3) 번역 (필요한 경우만)
    String translated = translate(result.getText(), targetLang, normalizedSource);

    // 4) Segment 객체 생성
    Segment segment = new Segment(
        index,
        chunk.getSize(),
        result.getText(),
        Instant.now(),
        result.getSegments(),
        normalizedSource,   // 내부 표현용 sourceLanguage (ko/en/ja 등)
        normalizedTarget,   // 내부 표현용 targetLanguage
        translated          // 번역 텍스트(없으면 null)
    );

    // 5) 메모리 세션에 저장
    SessionKey key = SessionKey.of(userIdx, meetingId);
    sessions
        .computeIfAbsent(key, k -> new ConcurrentSkipListMap<>())
        .put(segment.getIndex(), segment);

    // 6) DB(meetRTChunks)에 STT 청크 저장
    if (segment.getText() != null && !segment.getText().isEmpty()) {
      meetingSttService.saveChunk(
          meetingId,              // meetIdx
          segment.getIndex(),     // seq
          normalizedSource,       // langCode (예: "ko")
          segment.getText(),      // content (STT 텍스트)
          true                    // finalChunk: 일단 전체 청크 단위로 true 처리
      );
    }

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

  /**
   * 번역 유틸
   * @param text STT 원문
   * @param targetLang 프론트에서 온 타깃 언어 (예: "ko", "en-US" 등)
   * @param sourceLang 내부 normalized 소스 언어 (예: "ko", "en")
   */
  private String translate(String text, String targetLang, String sourceLang) {
    if (!StringUtils.hasText(text)) {
      return null;
    }
    // targetLang 명시 안 된 경우: 자동 한국어 번역
    if (!StringUtils.hasText(targetLang)) {
      return translationClient.translateToKoreanAuto(text).orElse(null);
    }

    String normalizedTarget = normalizeLanguage(targetLang);
    return translationClient.translate(text, sourceLang, normalizedTarget).orElse(null);
  }

  private String ensureKoreanText(Segment segment) {
    if (segment == null) {
      return null;
    }
    // 이미 한국어 번역 텍스트가 있고, 타깃 언어가 ko 계열이면 그대로 사용
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

  /**
   * Clova Speech API 가 요구하는 language 포맷으로 매핑
   * 허용: ko-KR, en-US, ja, enko, zh-cn, zh-tw
   */
  private String resolveClovaLanguage(String lang) {
    if (!StringUtils.hasText(lang)) {
      return null;  // null이면 ClovaSpeechClient 에서 기본값 사용
    }
    String normalized = lang.trim().toLowerCase().replace('_', '-');

    switch (normalized) {
      case "ko":
      case "ko-kr":
        return "ko-KR";
      case "en":
      case "en-us":
        return "en-US";
      case "ja":
        return "ja";
      case "enko":
        return "enko";
      case "zh-cn":
      case "zh-hans":
        return "zh-cn";
      case "zh-tw":
      case "zh-hant":
        return "zh-tw";
      default:
        // 잘못된 값이면 null 반환해서 기본 언어로 처리
        return null;
    }
  }

  /**
   * 내부 표현용 언어 정규화
   * "ko-KR" -> "ko", "en_US" -> "en" 이런 식으로 정리
   */
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
