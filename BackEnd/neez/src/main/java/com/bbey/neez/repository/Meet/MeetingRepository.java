package com.bbey.neez.repository.Meet;

import com.bbey.neez.entity.Meet.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {
    List<Meeting> findByUserIdx(Long userIdx);
}
