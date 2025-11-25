package com.bbey.neez.service.Meet;

import com.bbey.neez.client.OpenAiChatClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Slf4j
@Service
public class MeetingSummaryService {

  private final MeetingSpeechService speechService;
  private final OpenAiChatClient chatClient;

  public MeetingSummaryService(MeetingSpeechService speechService,
      OpenAiChatClient chatClient) {
    this.speechService = speechService;
    this.chatClient = chatClient;
  }

  /**
   * 1) 회의 음성 → Clova STT
   * 2) STT 결과 → OpenAI/Gemini 요약
   */
  public MeetingSummary summarize(Long userIdx, Long meetingId,
      MultipartFile audio,
      String sourceLanguage) throws Exception {

    log.debug("Summarizing meeting userIdx={}, meetingId={}, file={}",
        userIdx,
        meetingId,
        audio != null ? audio.getOriginalFilename() : null);

    // 1) STT
    MeetingSpeechService.TranscriptionResult transcription = speechService.transcribe(audio, sourceLanguage);

    String transcript = transcription.transcript();
    List<MeetingSpeechService.SpeakerTurn> speakerTurns = transcription.speakerTurns();

    // 2) 요약
    String summary;
    try {
      summary = chatClient.summarize(transcript);
    } catch (WebClientResponseException.TooManyRequests e) {
      log.warn("OpenAI 429 TooManyRequests meetingId={}", meetingId);
      summary = "요약 생성 요청이 많아 현재 자동 요약을 제공하지 못했습니다. 전체 회의록을 참고해 주세요.";
    } catch (WebClientResponseException e) {
      log.error("OpenAI 요약 호출 실패 (HTTP {}): {}",
          e.getRawStatusCode(), e.getResponseBodyAsString(), e);
      summary = "요약 생성 중 외부 요약 서비스 오류가 발생했습니다. 전체 회의록은 정상적으로 생성되었습니다.";
    } catch (Exception e) {
      log.error("Unexpected error while summarizing meetingId={}", meetingId, e);
      summary = "요약 생성 중 예기치 못한 오류가 발생했습니다. 전체 회의록은 정상적으로 생성되었습니다.";
    }

    return new MeetingSummary(transcript, summary, speakerTurns);
  }

  public static class MeetingSummary {
    private final String transcript;
    private final String summary;
    private final List<MeetingSpeechService.SpeakerTurn> speakerTurns;

    public MeetingSummary(String transcript,
        String summary,
        List<MeetingSpeechService.SpeakerTurn> speakerTurns) {
      this.transcript = transcript;
      this.summary = summary;
      this.speakerTurns = speakerTurns;
    }

    public String transcript() {
      return transcript;
    }

    public String summary() {
      return summary;
    }

    public List<MeetingSpeechService.SpeakerTurn> speakerTurns() {
      return speakerTurns;
    }
  }
}
