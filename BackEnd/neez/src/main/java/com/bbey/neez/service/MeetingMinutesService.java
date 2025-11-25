package com.bbey.neez.service;

import com.bbey.neez.client.OpenAiChatClient;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Service
public class MeetingMinutesService {

  private final MeetingSpeechStreamService streamService;
  private final OpenAiChatClient chatClient;

  public MeetingMinutesService(MeetingSpeechStreamService streamService,
                               OpenAiChatClient chatClient) {
    this.streamService = streamService;
    this.chatClient = chatClient;
  }

  public StreamMeetingMinutes finalizeStreamingMeeting(Long meetingId) {
    String originalTranscript = streamService.getTranscriptText(meetingId);
    String koreanTranscript = streamService.getKoreanTranscript(meetingId);
    String summary = summarizeInKorean(koreanTranscript);
    List<MeetingSpeechStreamService.Segment> segments = streamService.getSegments(meetingId);

    return new StreamMeetingMinutes(
        meetingId,
        originalTranscript,
        koreanTranscript,
        summary,
        segments
    );
  }

  private String summarizeInKorean(String transcript) {
    if (!StringUtils.hasText(transcript)) {
      return "요약할 대화 내용이 없습니다.";
    }

    try {
      return chatClient.summarize(transcript);
    } catch (WebClientResponseException.TooManyRequests e) {
      log.warn("Gemini/OpenAI rate limit while summarizing stream: {}", e.getResponseBodyAsString());
      return "요약 요청이 많아 일시적으로 요약을 생성하지 못했습니다. 전체 대화 기록을 확인해 주세요.";
    } catch (WebClientResponseException e) {
      log.error("Gemini/OpenAI summarization failed (HTTP {}): {}", e.getRawStatusCode(),
          e.getResponseBodyAsString(), e);
      return "요약을 생성하는 중 오류가 발생했습니다. 전체 대화 기록을 확인해 주세요.";
    } catch (Exception e) {
      log.error("Unexpected error during stream summary", e);
      return "요약을 생성하는 중 알 수 없는 오류가 발생했습니다. 전체 대화 기록을 확인해 주세요.";
    }
  }

  public static class StreamMeetingMinutes {
    private final Long meetingId;
    private final String originalTranscript;
    private final String koreanTranscript;
    private final String summary;
    private final List<MeetingSpeechStreamService.Segment> segments;

    public StreamMeetingMinutes(Long meetingId,
                                String originalTranscript,
                                String koreanTranscript,
                                String summary,
                                List<MeetingSpeechStreamService.Segment> segments) {
      this.meetingId = meetingId;
      this.originalTranscript = originalTranscript;
      this.koreanTranscript = koreanTranscript;
      this.summary = summary;
      this.segments = segments;
    }

    public Long getMeetingId() {
      return meetingId;
    }

    public String getOriginalTranscript() {
      return originalTranscript;
    }

    public String getKoreanTranscript() {
      return koreanTranscript;
    }

    public String getSummary() {
      return summary;
    }

    public List<MeetingSpeechStreamService.Segment> getSegments() {
      return segments;
    }
  }
}
