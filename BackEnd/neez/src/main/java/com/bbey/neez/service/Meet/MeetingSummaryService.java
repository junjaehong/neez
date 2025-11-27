package com.bbey.neez.service.Meet;

import com.bbey.neez.client.OpenAiChatClient;
import com.bbey.neez.entity.Meet.MeetShort;
import com.bbey.neez.entity.Meet.Meeting;
import com.bbey.neez.entity.Meet.MeetingParticipant;
import com.bbey.neez.repository.Meet.MeetRTChunkRepository;
import com.bbey.neez.repository.Meet.MeetShortRepository;
import com.bbey.neez.repository.Meet.MeetingParticipantRepository;
import com.bbey.neez.repository.Meet.MeetingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MeetingSummaryService {

    private final MeetingRepository meetingRepository;
    private final MeetRTChunkRepository chunkRepository;
    private final MeetShortRepository shortRepository;
    private final MeetingParticipantRepository participantRepository;
    private final OpenAiChatClient openAiChatClient;

    // 단일 파일 STT 용
    private final MeetingSpeechService speechService;

    /**
     * [스트리밍 회의용]
     * 회의 전체 STT chunk → Gemini 요약 → meetShorts 저장
     *
     * @param meetIdx 회의 PK
     * @param userIdx 요약 생성자(보통 회의 생성자, SecurityUtil 에서 가져온 현재 유저)
     * @return 요약 텍스트
     */
    public String summarize(Long meetIdx, Long userIdx) {

        // 1. 회의 정보 조회
        Meeting meeting = meetingRepository.findById(meetIdx)
                .orElseThrow(() -> new IllegalArgumentException("회의를 찾을 수 없습니다. id=" + meetIdx));

        // 2. STT chunk 전체 모아서 transcript 생성
        String transcript = chunkRepository.findByMeetIdxOrderBySeqAsc(meetIdx).stream()
                .map(c -> c.getContent())
                .reduce((a, b) -> a + "\n" + b)
                .orElse("");

        if (transcript.isEmpty()) {
            return "회의 내용(STT)이 없습니다.";
        }

        // 3. Gemini 요약 호출
        String summary = openAiChatClient.summarize(transcript);

        // 4. meetShorts 테이블에 저장
        Long ownerUserIdx = (userIdx != null) ? userIdx : meeting.getUserIdx();

        MeetShort meetShort = MeetShort.builder()
                .userIdx(ownerUserIdx)
                .title(meeting.getTitle())
                .shorts(summary)
                .summaryLang(meeting.getMainLang() != null ? meeting.getMainLang() : "ko")
                .audioUrl(meeting.getAudioUrl())
                .build();

        shortRepository.save(meetShort);

        // 5. 회의 참가자들의 명함 메모에 요약 붙이기는 MeetingMinutesService 에서 처리
        return summary;
    }

    /**
     * [단일 파일 업로드용]
     * 단일 회의 음성 파일 → STT → 요약 (DB 저장은 안 하고, 컨트롤러 응답용 DTO로 리턴)
     */
    public MeetingSummary summarize(Long userIdx,
                                    Long meetingId,
                                    MultipartFile audio,
                                    String sourceLanguage) throws Exception {

        // 1) STT
        MeetingSpeechService.TranscriptionResult transcription = speechService.transcribe(audio, sourceLanguage);

        String transcript = transcription.transcript();
        List<MeetingSpeechService.SpeakerTurn> speakerTurns = transcription.speakerTurns();

        // 2) Gemini 요약
        String summary = openAiChatClient.summarize(transcript);

        // 3) 컨트롤러 응답용 DTO로 리턴
        return new MeetingSummary(transcript, summary, speakerTurns);
    }

    /**
     * /{meetingId}/audio 응답용 DTO
     */
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
