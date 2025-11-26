package com.bbey.neez.repository.meet;

import com.bbey.neez.entity.meet.MeetingParticipant;
import com.bbey.neez.entity.meet.Meeting;
import com.bbey.neez.entity.BizCard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeetingParticipantRepository extends JpaRepository<MeetingParticipant, Long> {

    List<MeetingParticipant> findByMeeting(Meeting meeting);

    List<MeetingParticipant> findByBizCard(BizCard bizCard);

    boolean existsByMeetingAndBizCard(Meeting meeting, BizCard bizCard);

    void deleteByMeeting(Meeting meeting);
}
