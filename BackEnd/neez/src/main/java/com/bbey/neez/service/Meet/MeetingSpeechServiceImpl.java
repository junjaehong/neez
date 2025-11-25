package com.bbey.neez.service.Meet;

import com.bbey.neez.client.ClovaSpeechClient;
import com.bbey.neez.client.ClovaSpeechClient.SpeakerSegment;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

public class MeetingSpeechServiceImpl implements MeetingSpeechService {

  private final ClovaSpeechClient clovaSpeechClient;

  public MeetingSpeechServiceImpl(ClovaSpeechClient clovaSpeechClient) {
    this.clovaSpeechClient = clovaSpeechClient;
  }

  @Override
  public TranscriptionResult transcribe(MultipartFile file, String language) throws Exception {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("Audio file must not be empty.");
    }

    ClovaSpeechClient.ClovaResult result = StringUtils.hasText(language)
        ? clovaSpeechClient.recognize(file.getBytes(), resolveClovaLanguage(language))
        : clovaSpeechClient.recognize(file.getBytes());
    List<SpeakerTurn> speakerTurns = buildSpeakerTurns(result.getSegments());
    return new TranscriptionResult(result.getText(), speakerTurns);
  }

  private List<SpeakerTurn> buildSpeakerTurns(List<SpeakerSegment> segments) {
    if (segments == null || segments.isEmpty()) {
      return Collections.emptyList();
    }

    List<SpeakerSegment> ordered = new ArrayList<>(segments);
    ordered.sort(Comparator.comparing(seg -> seg.getStart() != null ? seg.getStart() : 0L));

    List<SpeakerTurn> turns = new ArrayList<>();
    StringBuilder currentText = new StringBuilder();
    String currentSpeaker = null;
    Long currentStart = null;
    Long currentEnd = null;

    for (SpeakerSegment segment : ordered) {
      String label = toSpeakerLabel(segment.getSpeakerLabel());

      if (!label.equals(currentSpeaker) && currentText.length() > 0) {
        turns.add(new SpeakerTurn(currentSpeaker, currentText.toString().trim(), currentStart, currentEnd));
        currentText.setLength(0);
        currentStart = null;
        currentEnd = null;
      }

      currentSpeaker = label;
      if (currentStart == null) {
        currentStart = segment.getStart();
      }
      currentEnd = segment.getEnd();

      if (segment.getText() != null && !segment.getText().trim().isEmpty()) {
        if (currentText.length() > 0) {
          currentText.append(' ');
        }
        currentText.append(segment.getText().trim());
      }
    }

    if (currentText.length() > 0) {
      turns.add(new SpeakerTurn(currentSpeaker, currentText.toString().trim(), currentStart, currentEnd));
    }

    return turns;
  }

  private String toSpeakerLabel(Integer label) {
    if (label == null) {
      return "UNKNOWN";
    }
    return "SPEAKER_" + label;
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
}
