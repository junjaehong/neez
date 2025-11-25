package com.bbey.neez.controller;

import com.bbey.neez.security.SecurityUtil;
import com.bbey.neez.service.Meet.MeetingMinutesService;
import com.bbey.neez.service.Meet.MeetingSpeechStreamService;
import com.bbey.neez.service.Meet.MeetingSummaryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/meetings/me")
@Tag(name = "Meeting Speech / STT API", description = "회의 음성 업로드 · STT · 요약 · 회의록 API\n\n" +
    "📌 meetingId란?\n" +
    "- 하나의 '회의 세션'을 구분하기 위한 ID입니다.\n" +
    "- Swagger에서는 임의의 숫자를 넣어 사용하면 됩니다. (예: 1)\n" +
    "- 같은 회의 동안에는 항상 같은 meetingId를 사용하세요.\n")
@SecurityRequirement(name = "BearerAuth")
public class MeetingSpeechController {

  private final MeetingSummaryService summaryService;
  private final MeetingMinutesService minutesService;
  private final MeetingSpeechStreamService streamService;

  public MeetingSpeechController(MeetingSummaryService summaryService,
      MeetingMinutesService minutesService,
      MeetingSpeechStreamService streamService) {
    this.summaryService = summaryService;
    this.minutesService = minutesService;
    this.streamService = streamService;
  }

  @Operation(summary = "단일 회의 음성 업로드 + STT + 요약", description = "하나의 전체 음성 파일을 업로드하여 STT와 요약을 수행합니다.\n" +
      "meetingId는 회의를 구분하는 임의의 숫자입니다. (예: 1)\n")
  @PostMapping("/{meetingId}/audio")
  public ResponseEntity<Map<String, Object>> upload(
      @Parameter(description = "회의 세션 ID (예: 1)") @PathVariable Long meetingId,
      @RequestPart("file") MultipartFile file,
      @Parameter(description = "원본 언어 코드 (예: ko)", example = "ko") @RequestParam(value = "sourceLang", required = false) String sourceLang)
      throws Exception {

    Long userIdx = SecurityUtil.getCurrentUserIdx();

    try {
      MeetingSummaryService.MeetingSummary result = summaryService.summarize(userIdx, meetingId, file, sourceLang);

      Map<String, Object> payload = new LinkedHashMap<>();
      payload.put("userIdx", userIdx);
      payload.put("meetingId", meetingId);
      payload.put("filename", file.getOriginalFilename());
      payload.put("bytes", file.getSize());
      payload.put("text", result.transcript());
      payload.put("summary", result.summary());
      payload.put("speakers", result.speakerTurns());

      return ResponseEntity.ok(payload);

    } catch (IllegalArgumentException ex) {
      return badRequest(meetingId, ex.getMessage());
    } catch (IllegalStateException ex) {
      return serviceUnavailable(meetingId, ex.getMessage());
    } catch (RuntimeException ex) {
      return internalError(meetingId, ex.getMessage());
    }
  }

  @Operation(summary = "회의 음성 청크(STT Streaming) 업로드", description = "음성을 여러 조각(chunk)으로 나누어 업로드하면서 실시간 STT/번역을 수행합니다.\n"
      +
      "같은 회의 도중에는 항상 같은 meetingId를 사용합니다. (예: 1)\n")
  @PostMapping("/{meetingId}/chunks")
  public ResponseEntity<Map<String, Object>> uploadChunk(
      @Parameter(description = "회의 세션 ID", example = "1") @PathVariable Long meetingId,
      @RequestPart("file") MultipartFile file,
      @Parameter(description = "청크 순번", example = "1") @RequestParam(value = "index", required = false) Long index,
      @Parameter(description = "타겟 번역 언어", example = "ko") @RequestParam(value = "targetLang", required = false) String targetLang,
      @Parameter(description = "원본 음성 언어", example = "ko") @RequestParam(value = "sourceLang", required = false) String sourceLang)
      throws Exception {

    Long userIdx = SecurityUtil.getCurrentUserIdx();

    try {
      MeetingSpeechStreamService.Segment segment = streamService.processChunk(userIdx, meetingId, index, file,
          targetLang, sourceLang);

      Map<String, Object> payload = new LinkedHashMap<>();
      payload.put("userIdx", userIdx);
      payload.put("meetingId", meetingId);
      payload.put("index", segment.getIndex());
      payload.put("text", segment.getText());
      payload.put("receivedAt", segment.getReceivedAt());
      payload.put("bytes", segment.getBytes());
      payload.put("sourceLanguage", segment.getSourceLanguage());
      payload.put("targetLanguage", segment.getTargetLanguage());
      payload.put("translation", segment.getTranslatedText());
      payload.put("transcript", streamService.getTranscriptText(userIdx, meetingId));
      payload.put("segments", streamService.getSegments(userIdx, meetingId));

      return ResponseEntity.ok(payload);

    } catch (IllegalArgumentException ex) {
      return badRequest(meetingId, ex.getMessage());
    } catch (IllegalStateException ex) {
      return serviceUnavailable(meetingId, ex.getMessage());
    } catch (RuntimeException ex) {
      return internalError(meetingId, ex.getMessage());
    }
  }

  @Operation(summary = "현재까지의 transcript 조회", description = "누적된 transcript(원본 텍스트)와 segments 목록을 조회합니다.\n" +
      "meetingId는 동일 회의의 ID여야 합니다.\n")
  @GetMapping("/{meetingId}/transcript")
  public ResponseEntity<Map<String, Object>> getTranscript(
      @Parameter(description = "회의 세션 ID", example = "1") @PathVariable Long meetingId) {
    Long userIdx = SecurityUtil.getCurrentUserIdx();

    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("userIdx", userIdx);
    payload.put("meetingId", meetingId);
    payload.put("transcript", streamService.getTranscriptText(userIdx, meetingId));
    payload.put("segments", streamService.getSegments(userIdx, meetingId));

    return ResponseEntity.ok(payload);
  }

  @Operation(summary = "회의 종료 후 회의록 생성", description = "지금까지 업로드된 음성 청크들을 바탕으로\n" +
      "- 원본 transcript\n" +
      "- 한국어 번역 transcript\n" +
      "- 요약(summary)\n" +
      "- segment 목록\n" +
      "등을 생성하여 반환합니다.\n")
  @PostMapping("/{meetingId}/minutes")
  public ResponseEntity<Map<String, Object>> finalizeStreamingMeeting(
      @Parameter(description = "회의 세션 ID", example = "1") @PathVariable Long meetingId) {
    Long userIdx = SecurityUtil.getCurrentUserIdx();

    try {
      MeetingMinutesService.StreamMeetingMinutes minutes = minutesService.finalizeStreamingMeeting(userIdx, meetingId);

      Map<String, Object> payload = new LinkedHashMap<>();
      payload.put("userIdx", userIdx);
      payload.put("meetingId", minutes.getMeetingId());
      payload.put("originalTranscript", minutes.getOriginalTranscript());
      payload.put("koreanTranscript", minutes.getKoreanTranscript());
      payload.put("summary", minutes.getSummary());
      payload.put("segments", minutes.getSegments());
      return ResponseEntity.ok(payload);

    } catch (IllegalArgumentException ex) {
      return badRequest(meetingId, ex.getMessage());
    } catch (IllegalStateException ex) {
      return serviceUnavailable(meetingId, ex.getMessage());
    } catch (RuntimeException ex) {
      return internalError(meetingId, ex.getMessage());
    }
  }

  /* ===== 공통 에러 처리 ===== */

  private ResponseEntity<Map<String, Object>> badRequest(Long meetingId, String msg) {
    Map<String, Object> error = new LinkedHashMap<>();
    error.put("meetingId", meetingId);
    error.put("error", "BAD_REQUEST");
    error.put("message", msg);
    return ResponseEntity.badRequest().body(error);
  }

  private ResponseEntity<Map<String, Object>> serviceUnavailable(Long meetingId, String msg) {
    Map<String, Object> error = new LinkedHashMap<>();
    error.put("meetingId", meetingId);
    error.put("error", "SERVICE_UNAVAILABLE");
    error.put("message", msg);
    return ResponseEntity.status(503).body(error);
  }

  private ResponseEntity<Map<String, Object>> internalError(Long meetingId, String msg) {
    Map<String, Object> error = new LinkedHashMap<>();
    error.put("meetingId", meetingId);
    error.put("error", "INTERNAL_ERROR");
    error.put("message", msg);
    return ResponseEntity.status(500).body(error);
  }
}
