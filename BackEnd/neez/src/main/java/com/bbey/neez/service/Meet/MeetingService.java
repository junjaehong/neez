package com.bbey.neez.service.Meet;

import com.bbey.neez.entity.Meet.Meeting;
import com.bbey.neez.repository.Meet.MeetingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MeetingService {

    private final MeetingRepository meetingRepository;

    /**
     * 회의 생성 (회의 시작)
     * title 자동생성: [ yyyy.MM.dd.THH:mm:ss ]
     */
    public Meeting createMeeting(Long userIdx, String mainLang) {

        String autoTitle = "[" +
                LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("yyyy.MM.dd.'T'HH:mm:ss"))
                + "]";

        Meeting meeting = Meeting.builder()
                .userIdx(userIdx)
                .title(autoTitle)
                .description(null)
                .mainLang(mainLang)
                .audioUrl(null)
                .startedAt(LocalDateTime.now())
                .status("ONGOING")
                .build();

        return meetingRepository.save(meeting);
    }

    /**
     * 회의 시작 API에서 사용하는 편의 메서드
     */
    public Meeting startMeeting(Long userIdx, String mainLang) {
        return createMeeting(userIdx, mainLang);
    }

    /**
     * 회의 종료 처리
     */
    public Meeting endMeeting(Long meetIdx) {
        Meeting meeting = meetingRepository.findById(meetIdx)
                .orElseThrow(() -> new IllegalArgumentException("회의를 찾을 수 없습니다. id=" + meetIdx));

        meeting.setEndedAt(LocalDateTime.now());
        meeting.setStatus("FINISHED");

        return meetingRepository.save(meeting);
    }

    /**
     * 사용자별 회의 목록 조회
     */
    @Transactional(readOnly = true)
    public List<Meeting> getMeetingsByUser(Long userIdx) {
        return meetingRepository.findByUserIdx(userIdx);
    }

    /**
     * 단건 조회
     */
    @Transactional(readOnly = true)
    public Meeting getMeeting(Long meetIdx) {
        return meetingRepository.findById(meetIdx)
                .orElseThrow(() -> new IllegalArgumentException("회의를 찾을 수 없습니다. id=" + meetIdx));
    }
}
