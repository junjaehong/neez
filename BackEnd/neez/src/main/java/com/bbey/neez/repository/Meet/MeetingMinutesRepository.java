package com.bbey.neez.repository.Meet;

import com.bbey.neez.entity.Meet.Meeting;
import com.bbey.neez.entity.Meet.MeetingMinutes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MeetingMinutesRepository extends JpaRepository<MeetingMinutes, Long> {

       // 특정 Meeting 엔티티로 단건 조회 (있다면)
       Optional<MeetingMinutes> findByMeeting(Meeting meeting);

       // 특정 명함과 연결된 회의록들
       List<MeetingMinutes> findAllByBizCard_Idx(Long bizCardIdx);

       // ✅ 사용자 기준 회의록 리스트 (최신순) - Meeting.userIdx 기준
       @Query("select mm " +
                     "from MeetingMinutes mm " +
                     "where mm.meeting.userIdx = :userIdx " +
                     "order by mm.createdAt desc")
       List<MeetingMinutes> findByUserIdxOrderByCreatedAtDesc(@Param("userIdx") Long userIdx);

       // ✅ meetingId + userIdx 기준 회의록 단건
       @Query("select mm " +
                     "from MeetingMinutes mm " +
                     "where mm.meeting.userIdx = :userIdx " +
                     "and mm.meeting.idx = :meetingId")
       Optional<MeetingMinutes> findByUserIdxAndMeetingId(@Param("userIdx") Long userIdx,
                     @Param("meetingId") Long meetingId);
}
