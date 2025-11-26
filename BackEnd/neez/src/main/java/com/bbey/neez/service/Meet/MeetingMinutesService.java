package com.bbey.neez.service.Meet;

import com.bbey.neez.client.OpenAiChatClient;
import com.bbey.neez.service.BizCard.BizCardMemoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
public class MeetingMinutesService {

  private final MeetingSpeechStreamService streamService;
  private final OpenAiChatClient chatClient;
  private final BizCardMemoService bizCardMemoService;

  public MeetingMinutesService(MeetingSpeechStreamService streamService,
                               OpenAiChatClient chatClient,
                               BizCardMemoService bizCardMemoService) {
    this.streamService = streamService;
    this.chatClient = chatClient;
    this.bizCardMemoService = bizCardMemoService;
  }

  /**
   * /meetings/me/{meetingId}/minutes
   * 스트리밍 회의에 대한 최종 회의록 생성
   *  - 전체 원문 transcript
   *  - 한국어 기준 transcript (번역 포함)
   *  - 요약(summary)
   *  - segment 목록
   * 을 구성해서 반환하고,
   * bizCardId가 넘어온 경우 해당 명함의 메모에도 요약을 append 한다.
   */
  public StreamMeetingMinutes finalizeStreamingMeeting(Long userIdx, Long meetingId, Long bizCardId) {
    String originalTranscript = streamService.getTranscriptText(userIdx, meetingId);
    String koreanTranscript = streamService.getKoreanTranscript(userIdx, meetingId);
    String summary = summarizeInKorean(koreanTranscript);
    List<MeetingSpeechStreamService.Segment> segments = streamService.getSegments(userIdx, meetingId);

    // 명함 메모에 회의 요약 추가 (선택)
    if (bizCardId != null && StringUtils.hasText(summary)) {
      appendSummaryToBizCardMemo(bizCardId, summary);
    }

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
      log.warn("요약 API 호출이 너무 많습니다 (429): {}", e.getMessage());
      return "요약 요청이 너무 많습니다. 잠시 후 다시 시도해주세요.";
    } catch (WebClientResponseException e) {
      log.warn("요약 API 호출 실패: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
      return "요약 생성 중 오류가 발생했습니다. (상태 코드: " + e.getStatusCode().value() + ")";
    } catch (Exception e) {
      log.error("요약 생성 중 알 수 없는 오류", e);
      return "요약 생성 중 알 수 없는 오류가 발생했습니다.";
    }
  }

  /**
   * 요약문을 지정된 명함 메모에 추가한다.
   *
   * 예시 포맷:
   * [2025.11.26.15:00:00]
   * - 사람들의 정보에 대해서 물어봄.
   * - 다양한 사람들은 다양한 종류가 있다고 함.
   */
  private void appendSummaryToBizCardMemo(Long bizCardId, String summary) {
    try {
      String existing = bizCardMemoService.getBizCardMemoContent(bizCardId);
      if (existing == null) {
        existing = "";
      }

      String timestamp = LocalDateTime.now()
          .format(DateTimeFormatter.ofPattern("yyyy.MM.dd.HH:mm:ss"));

      StringBuilder sb = new StringBuilder(existing);
      if (!existing.endsWith("\n") && existing.length() > 0) {
        sb.append("\n");
      }
      sb.append("[")
        .append(timestamp)
        .append("]\n");

      for (String line : summary.split("\\r?\\n")) {
        if (StringUtils.hasText(line)) {
          sb.append("- ").append(line.trim()).append("\n");
        }
      }
      sb.append("\n");

      bizCardMemoService.updateBizCardMemo(bizCardId, sb.toString());
    } catch (IOException e) {
      log.warn("명함 메모 읽기/쓰기 실패 (bizCardId={}): {}", bizCardId, e.getMessage());
    } catch (RuntimeException e) {
      log.warn("명함 메모 업데이트 중 오류 (bizCardId={})", bizCardId, e);
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
