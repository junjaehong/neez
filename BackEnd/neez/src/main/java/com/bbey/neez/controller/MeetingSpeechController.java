package com.bbey.neez.controller;

import com.bbey.neez.service.MeetingSpeechService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@RestController
@RequestMapping("/speech/meetings")
public class MeetingSpeechController {

  private final MeetingSpeechService svc;
  public MeetingSpeechController(MeetingSpeechService svc) { this.svc = svc; }

  @PostMapping("/{id}/audio")
  public ResponseEntity<Map<String, Object>> upload(
      @PathVariable Long id,
      @RequestPart("file") MultipartFile file) throws Exception {

    String text = svc.transcribe(file);
    return ResponseEntity.ok(Map.of(
        "meetingId", id,
        "filename", file.getOriginalFilename(),
        "bytes", file.getSize(),
        "text", text
    ));
  }
}
