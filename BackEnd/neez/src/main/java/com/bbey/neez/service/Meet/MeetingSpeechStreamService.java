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

    // 3) 번역 (한국어/설정 언어 동시 처리)
    // targetLang은 Papago가 인식할 수 있도록 원본 값을 그대로 전달
    TranslationPair translations = translateDual(result.getText(), targetLang, normalizedSource);

    // 4) Segment 객체 생성
    Segment segment = new Segment(
        index,
        chunk.getSize(),
        result.getText(),
        Instant.now(),
        result.getSegments(),
        normalizedSource,   // 내부 표현용 sourceLanguage (ko/en/ja 등)
        normalizedTarget,   // 내부 표현용 targetLanguage
        translations.displayText(),          // 기본 노출 번역
        translations.toKorean(),             // 한국어 번역
        translations.toTarget()              // 타깃 언어 번역
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
   * 지금까지 모은 번역 텍스트(target 언어 기준)를 하나로 이어붙임
   */
  public String getTranslatedTranscript(Long userIdx, Long meetingId) {
    SessionKey key = SessionKey.of(userIdx, meetingId);
    ConcurrentSkipListMap<Long, Segment> map = sessions.get(key);
    if (map == null || map.isEmpty()) {
      return "";
    }

    StringBuilder sb = new StringBuilder();
    for (Segment segment : map.values()) {
      String text = segment.getTranslatedText();
      if (!StringUtils.hasText(text)) {
        // 타깃 번역이 비어 있으면 다른 변환본을 우선 활용
        text = segment.getTranslatedToTarget();
      }
      if (!StringUtils.hasText(text)) {
        text = segment.getTranslatedToKorean();
      }
      if (!StringUtils.hasText(text) && StringUtils.hasText(segment.getText())) {
        // 그래도 없으면 한국어 자동 번역 시도
        text = translationClient.translateToKoreanAuto(segment.getText()).orElse(null);
      }
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
  /**
   * 한국어/타깃 언어 동시 번역 처리
   */
  private TranslationPair translateDual(String text, String targetLang, String sourceLang) {
    if (!StringUtils.hasText(text)) {
      return TranslationPair.empty();
    }

    String normalizedSource = normalizeLanguage(sourceLang);
    boolean hasHangul = containsHangul(text);
    boolean hasEnglish = containsEnglish(text);

    // 한국어 번역은 항상 시도 (영어/기타 언어 포함 대비)
    String toKorean = translationClient.translateToKoreanAuto(text).orElse(null);

    // 타깃 번역: 한국어가 포함되어 있고 타깃 언어가 있을 때만
    String toTarget = null;
    if (hasHangul && StringUtils.hasText(targetLang)) {
      // Papago 클라이언트에서 언어코드 매핑 처리
      toTarget = translationClient.translate(text, normalizedSource, targetLang).orElse(null);
    }

    // 기본 표시 텍스트: 영어가 섞여 있으면 toKorean, 아니면 타깃/한국어 우선순위
    String display = hasEnglish
        ? toKorean
        : (toTarget != null ? toTarget : toKorean);

    return new TranslationPair(display, toKorean, toTarget);
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

  private boolean containsHangul(String text) {
    if (!StringUtils.hasText(text)) {
      return false;
    }
    return text.chars().anyMatch(ch -> (ch >= 0xAC00 && ch <= 0xD7AF) || (ch >= 0x3130 && ch <= 0x318F));
  }

  private boolean containsEnglish(String text) {
    if (!StringUtils.hasText(text)) {
      return false;
    }
    return text.chars().anyMatch(ch ->
        (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z'));
  }

  /**
   * 한국어/타깃 번역 쌍
   */
  private static class TranslationPair {
    private final String displayText;
    private final String toKorean;
    private final String toTarget;

    private TranslationPair(String displayText, String toKorean, String toTarget) {
      this.displayText = displayText;
      this.toKorean = toKorean;
      this.toTarget = toTarget;
    }

    public static TranslationPair empty() {
      return new TranslationPair(null, null, null);
    }

    public String displayText() {
      return displayText;
    }

    public String toKorean() {
      return toKorean;
    }

    public String toTarget() {
      return toTarget;
    }
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
    private final String translatedToKorean;
    private final String translatedToTarget;

    public Segment(long index,
                   long bytes,
                   String text,
                   Instant receivedAt,
                   List<SpeakerSegment> speakerSegments,
                   String sourceLanguage,
                   String targetLanguage,
                   String translatedText,
                   String translatedToKorean,
                   String translatedToTarget) {
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
      this.translatedToKorean = translatedToKorean;
      this.translatedToTarget = translatedToTarget;
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

    public String getTranslatedToKorean() {
      return translatedToKorean;
    }

    public String getTranslatedToTarget() {
      return translatedToTarget;
    }
  }
}
