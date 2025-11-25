package com.bbey.neez.controller;

import com.bbey.neez.service.MeetingMinutesService;
import com.bbey.neez.service.MeetingSummaryService;
import com.bbey.neez.service.MeetingSpeechStreamService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/meetings")
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

  @PostMapping("/{id}/audio")
  public ResponseEntity<Map<String, Object>> upload(
      @PathVariable Long id,
      @RequestPart("file") MultipartFile file,
      @RequestParam(value = "sourceLang", required = false) String sourceLang) throws Exception {

    try {
      MeetingSummaryService.MeetingSummary result = summaryService.summarize(file, sourceLang);

      Map<String, Object> payload = new LinkedHashMap<>();
      payload.put("meetingId", id);
      payload.put("filename", file.getOriginalFilename());
      payload.put("bytes", file.getSize());
      payload.put("text", result.transcript());
      payload.put("summary", result.summary());
      payload.put("speakers", result.speakerTurns());

      return ResponseEntity.ok(payload);
    } catch (IllegalArgumentException ex) {
      Map<String, Object> error = new LinkedHashMap<>();
      error.put("meetingId", id);
      error.put("error", "BAD_REQUEST");
      error.put("message", ex.getMessage());
      return ResponseEntity.badRequest().body(error);
    } catch (IllegalStateException ex) {
      Map<String, Object> error = new LinkedHashMap<>();
      error.put("meetingId", id);
      error.put("error", "SERVICE_UNAVAILABLE");
      error.put("message", ex.getMessage());
      return ResponseEntity.status(503).body(error);
    } catch (RuntimeException ex) {
      Map<String, Object> error = new LinkedHashMap<>();
      error.put("meetingId", id);
      error.put("error", "INTERNAL_ERROR");
      error.put("message", ex.getMessage());
      return ResponseEntity.status(500).body(error);
    }
  }

  @PostMapping("/{id}/chunks")
  public ResponseEntity<Map<String, Object>> uploadChunk(
      @PathVariable Long id,
      @RequestPart("file") MultipartFile file,
      @RequestParam(value = "index", required = false) Long index,
      @RequestParam(value = "targetLang", required = false) String targetLang,
      @RequestParam(value = "sourceLang", required = false) String sourceLang) throws Exception {

    try {
      MeetingSpeechStreamService.Segment segment =
          streamService.processChunk(id, index, file, targetLang, sourceLang);

      Map<String, Object> payload = new LinkedHashMap<>();
      payload.put("meetingId", id);
      payload.put("index", segment.getIndex());
      payload.put("text", segment.getText());
      payload.put("receivedAt", segment.getReceivedAt());
      payload.put("bytes", segment.getBytes());
      payload.put("sourceLanguage", segment.getSourceLanguage());
      payload.put("targetLanguage", segment.getTargetLanguage());
      payload.put("translation", segment.getTranslatedText());
      payload.put("transcript", streamService.getTranscriptText(id));
      payload.put("segments", streamService.getSegments(id));

      return ResponseEntity.ok(payload);
    } catch (IllegalArgumentException ex) {
      Map<String, Object> error = new LinkedHashMap<>();
      error.put("meetingId", id);
      error.put("error", "BAD_REQUEST");
      error.put("message", ex.getMessage());
      return ResponseEntity.badRequest().body(error);
    } catch (IllegalStateException ex) {
      Map<String, Object> error = new LinkedHashMap<>();
      error.put("meetingId", id);
      error.put("error", "SERVICE_UNAVAILABLE");
      error.put("message", ex.getMessage());
      return ResponseEntity.status(503).body(error);
    } catch (RuntimeException ex) {
      Map<String, Object> error = new LinkedHashMap<>();
      error.put("meetingId", id);
      error.put("error", "INTERNAL_ERROR");
      error.put("message", ex.getMessage());
      return ResponseEntity.status(500).body(error);
    }
  }

  @GetMapping("/{id}/transcript")
  public ResponseEntity<Map<String, Object>> getTranscript(@PathVariable Long id) {
    Map<String, Object> payload = new LinkedHashMap<>();
    payload.put("meetingId", id);
    payload.put("transcript", streamService.getTranscriptText(id));
    payload.put("segments", streamService.getSegments(id));
    return ResponseEntity.ok(payload);
  }

  @PostMapping("/{id}/minutes")
  public ResponseEntity<Map<String, Object>> finalizeStreamingMeeting(@PathVariable Long id) {
    try {
      MeetingMinutesService.StreamMeetingMinutes minutes = minutesService.finalizeStreamingMeeting(id);

      Map<String, Object> payload = new LinkedHashMap<>();
      payload.put("meetingId", minutes.getMeetingId());
      payload.put("originalTranscript", minutes.getOriginalTranscript());
      payload.put("koreanTranscript", minutes.getKoreanTranscript());
      payload.put("summary", minutes.getSummary());
      payload.put("segments", minutes.getSegments());

      return ResponseEntity.ok(payload);
    } catch (IllegalArgumentException ex) {
      Map<String, Object> error = new LinkedHashMap<>();
      error.put("meetingId", id);
      error.put("error", "BAD_REQUEST");
      error.put("message", ex.getMessage());
      return ResponseEntity.badRequest().body(error);
    } catch (IllegalStateException ex) {
      Map<String, Object> error = new LinkedHashMap<>();
      error.put("meetingId", id);
      error.put("error", "SERVICE_UNAVAILABLE");
      error.put("message", ex.getMessage());
      return ResponseEntity.status(503).body(error);
    } catch (RuntimeException ex) {
      Map<String, Object> error = new LinkedHashMap<>();
      error.put("meetingId", id);
      error.put("error", "INTERNAL_ERROR");
      error.put("message", ex.getMessage());
      return ResponseEntity.status(500).body(error);
    }
  }
}
