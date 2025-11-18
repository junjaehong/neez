package com.bbey.neez.service;

import java.util.Collections;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface MeetingSpeechService {
  TranscriptionResult transcribe(MultipartFile file, String language) throws Exception;

  class TranscriptionResult {
    private final String transcript;
    private final List<SpeakerTurn> speakerTurns;

    public TranscriptionResult(String transcript, List<SpeakerTurn> speakerTurns) {
      this.transcript = transcript;
      this.speakerTurns = speakerTurns != null ? speakerTurns : Collections.emptyList();
    }

    public String transcript() {
      return transcript;
    }

    public List<SpeakerTurn> speakerTurns() {
      return speakerTurns;
    }

    public String getTranscript() {
      return transcript;
    }

    public List<SpeakerTurn> getSpeakerTurns() {
      return speakerTurns;
    }
  }

  class SpeakerTurn {
    private final String speaker;
    private final String text;
    private final Long start;
    private final Long end;

    public SpeakerTurn(String speaker, String text, Long start, Long end) {
      this.speaker = speaker;
      this.text = text;
      this.start = start;
      this.end = end;
    }

    public String speaker() {
      return speaker;
    }

    public String text() {
      return text;
    }

    public Long start() {
      return start;
    }

    public Long end() {
      return end;
    }

    public String getSpeaker() {
      return speaker;
    }

    public String getText() {
      return text;
    }

    public Long getStart() {
      return start;
    }

    public Long getEnd() {
      return end;
    }
  }
}
