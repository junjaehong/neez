package com.bbey.neez.repository.Meet;

import com.bbey.neez.entity.Meet.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    // user 기준 회의 목록 조회
    List<Meeting> findByUserIdx(Long userIdx);
}
