package com.bbey.neez.repository.Meet;

import com.bbey.neez.entity.Meet.MeetingParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeetingParticipantRepository extends JpaRepository<MeetingParticipant, Long> {

    List<MeetingParticipant> findByMeetIdx(Long meetIdx);
}
