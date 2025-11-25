package com.bbey.neez.service;

import com.bbey.neez.client.OpenAiChatClient;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClientResponseException;

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
   * 1) 회의 음성 → Clova STT로 전체 transcript 생성
   * 2) transcript → OpenAI로 요약 시도
   *    - 429(Too Many Requests) 등으로 요약 실패해도 전체 API를 죽이지 않고,
   *      transcript는 그대로 반환하고 summary에 오류 메시지를 넣어 준다.
   */
  public MeetingSummary summarize(MultipartFile audio, String sourceLanguage) throws Exception {
    // ✅ 1. STT (여기서 에러 나면 컨트롤러에서 처리하는 게 맞음)
    MeetingSpeechService.TranscriptionResult transcription = speechService.transcribe(audio, sourceLanguage);
    String transcript = transcription.transcript();
    List<MeetingSpeechService.SpeakerTurn> speakerTurns = transcription.speakerTurns();

    // ✅ 2. 요약 (실패해도 transcript는 살려서 응답)
    String summary;
    try {
      summary = chatClient.summarize(transcript);
    } catch (WebClientResponseException.TooManyRequests e) {
      // OpenAI 429: 사용량 제한 / 레이트 리밋
      log.warn("OpenAI 429 TooManyRequests: {}", e.getResponseBodyAsString());
      summary = "요약 생성 서비스 요청이 많아 현재 자동 요약을 제공하지 못했습니다. " +
                "아래 전체 회의록을 참고해 주세요.";
    } catch (WebClientResponseException e) {
      // 다른 HTTP 오류
      log.error("OpenAI 요약 호출 실패 (HTTP {}): {}", e.getRawStatusCode(), e.getResponseBodyAsString(), e);
      summary = "요약 생성 중 외부 요약 서비스 오류가 발생했습니다. 전체 회의록은 정상적으로 생성되었습니다.";
    } catch (Exception e) {
      // 그 외 모든 예외 보호
      log.error("OpenAI 요약 호출 중 예기치 못한 오류", e);
      summary = "요약 생성 중 예기치 못한 오류가 발생했습니다. 전체 회의록은 정상적으로 생성되었습니다.";
    }

    return new MeetingSummary(transcript, summary, speakerTurns);
  }

  public static class MeetingSummary {
    private final String transcript;
    private final String summary;
    private final List<MeetingSpeechService.SpeakerTurn> speakerTurns;

    public MeetingSummary(String transcript, String summary,
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

