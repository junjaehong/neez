package com.bbey.neez.service.Meet;

import com.bbey.neez.entity.Meet.MeetRTChunk;
import com.bbey.neez.entity.Meet.Meeting;
import com.bbey.neez.entity.Meet.MeetingParticipant;
import com.bbey.neez.repository.Meet.MeetRTChunkRepository;
import com.bbey.neez.repository.Meet.MeetingParticipantRepository;
import com.bbey.neez.service.BizCard.BizCardMemoServiceImpl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MeetingMinutesService {

  private final MeetingService meetingService;
  private final MeetingSummaryService meetingSummaryService;
  private final MeetRTChunkRepository chunkRepository;
  private final MeetingParticipantRepository meetingParticipantRepository;
  private final BizCardMemoServiceImpl bizCardMemoService;

  /**
   * 스트리밍 회의 종료 + 최종 회의록 생성 + 명함 메모 업데이트
   *
   * @param userIdx   현재 유저
   * @param meetIdx   회의 ID
   * @param bizCardId 특정 명함 한 개에만 붙이고 싶을 때 사용(옵션). null이면 참석자 전원에 붙임.
   */
  public StreamMeetingMinutes finalizeStreamingMeeting(Long userIdx,
      Long meetIdx,
      Long bizCardId) {

    // 1) 회의 종료 처리 (endedAt, status=FINISHED)
    Meeting meeting = meetingService.endMeeting(meetIdx);

    // 2) STT 청크 기반 원본 transcript 구성
    List<MeetRTChunk> chunks = chunkRepository.findByMeetIdxOrderBySeqAsc(meetIdx);

    String originalTranscript = chunks.stream()
        .map(MeetRTChunk::getContent)
        .reduce("", (a, b) -> a.isEmpty() ? b : a + "\n" + b);

    // 3) 번역본 (지금은 별도 meetTranslations를 안 쓰고, 일단 원본과 동일하게)
    String koreanTranscript = originalTranscript;
    // TODO: meetTranslations에서 lang_code='ko'인 번역 결과를 읽어와서 대체 가능

    // 4) 요약 생성 (MeetShort 저장까지 포함)
    String summary = meetingSummaryService.summarize(meetIdx, userIdx);

    // 5) 명함 메모 업데이트
    if (bizCardId != null) {
      // 지정된 명함 한 개만
      bizCardMemoService.appendMeetingSummaryToBizCard(bizCardId, meeting.getTitle(), summary);
    } else {
      // 회의 참석자 전원
      List<MeetingParticipant> participants = meetingParticipantRepository.findByMeetIdx(meetIdx);
      for (MeetingParticipant p : participants) {
        bizCardMemoService.appendMeetingSummaryToBizCard(p.getBizcardIdx(), meeting.getTitle(), summary);
      }
    }

    // 6) 프론트 응답용 segment 뷰 만들기
    List<SegmentView> segmentViews = chunks.stream()
        .map(c -> new SegmentView(
            c.getSeq(),
            c.getChunkType(),
            c.getLangCode(),
            c.getContent(),
            "FINAL".equalsIgnoreCase(c.getChunkType()), // 🔥 여기 수정
            c.getCreatedAt()))
        .collect(Collectors.toList());

    return new StreamMeetingMinutes(
        meetIdx,
        originalTranscript,
        koreanTranscript,
        summary,
        segmentViews);
  }

  // ======================= DTO =======================

  @Getter
  public static class StreamMeetingMinutes {
    private final Long meetingId;
    private final String originalTranscript;
    private final String koreanTranscript;
    private final String summary;
    private final List<SegmentView> segments;

    public StreamMeetingMinutes(Long meetingId,
        String originalTranscript,
        String koreanTranscript,
        String summary,
        List<SegmentView> segments) {
      this.meetingId = meetingId;
      this.originalTranscript = originalTranscript;
      this.koreanTranscript = koreanTranscript;
      this.summary = summary;
      this.segments = segments;
    }
  }

  @Getter
  public static class SegmentView {
    private final Long index;
    private final String chunkType;
    private final String langCode;
    private final String text;
    private final boolean isFinal;
    private final LocalDateTime createdAt;

    public SegmentView(Long index,
        String chunkType,
        String langCode,
        String text,
        boolean isFinal,
        LocalDateTime createdAt) {
      this.index = index;
      this.chunkType = chunkType;
      this.langCode = langCode;
      this.text = text;
      this.isFinal = isFinal;
      this.createdAt = createdAt;
    }
  }
}
