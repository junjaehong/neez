package com.bbey.neez.service;

import com.bbey.neez.client.OpenAiChatClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

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

  /**
   * /meetings/me/{meetingId}/minutes
   */
  public StreamMeetingMinutes finalizeStreamingMeeting(Long userIdx, Long meetingId) {
    String originalTranscript = streamService.getTranscriptText(userIdx, meetingId);
    String koreanTranscript = streamService.getKoreanTranscript(userIdx, meetingId);
    String summary = summarizeInKorean(koreanTranscript);
    List<MeetingSpeechStreamService.Segment> segments = streamService.getSegments(userIdx, meetingId);

    return new StreamMeetingMinutes(
        meetingId,
        originalTranscript,
        koreanTranscript,
        summary,
        segments);
  }

  private String summarizeInKorean(String transcript) {
    if (!StringUtils.hasText(transcript)) {
      return "요약할 대화 내용이 없습니다.";
    }

    try {
      return chatClient.summarize(transcript);
    } catch (WebClientResponseException.TooManyRequests e) {
      log.warn("요약 요청 과부하 (429 TooManyRequests)", e);
      return "요약 서비스 요청이 많아 현재 자동 요약을 제공하지 못했습니다. 전체 회의록을 참고해 주세요.";
    } catch (WebClientResponseException e) {
      log.error("요약 HTTP {} 오류: {}", e.getRawStatusCode(), e.getResponseBodyAsString(), e);
      return "요약 생성 중 외부 요약 서비스 오류가 발생했습니다.";
    } catch (Exception e) {
      log.error("요약 생성 중 알 수 없는 오류", e);
      return "요약 생성 중 알 수 없는 오류가 발생했습니다.";
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
