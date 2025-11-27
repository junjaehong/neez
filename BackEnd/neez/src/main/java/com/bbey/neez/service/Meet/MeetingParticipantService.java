package com.bbey.neez.service.Meet;

import com.bbey.neez.entity.Meet.MeetingParticipant;
import com.bbey.neez.repository.Meet.MeetingParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MeetingParticipantService {

    private final MeetingParticipantRepository meetingParticipantRepository;

    /**
     * 특정 회의(meetIdx)의 참석자 명함 목록을 재설정
     * - 기존 참석자 삭제 후 새로 저장
     */
    public void setParticipants(Long meetIdx, List<Long> bizCardIds) {
        meetingParticipantRepository.deleteByMeetIdx(meetIdx);

        if (bizCardIds == null || bizCardIds.isEmpty()) {
            return;
        }

        List<MeetingParticipant> participants = bizCardIds.stream()
                .map(bizId -> MeetingParticipant.builder()
                        .meetIdx(meetIdx)
                        .bizcardIdx(bizId)
                        .build())
                .collect(Collectors.toList());

        meetingParticipantRepository.saveAll(participants);
    }

    @Transactional(readOnly = true)
    public List<MeetingParticipant> getParticipants(Long meetIdx) {
        return meetingParticipantRepository.findByMeetIdx(meetIdx);
    }
}
