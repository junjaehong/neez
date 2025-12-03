package com.bbey.neez.repository.Meet;

import com.bbey.neez.entity.Meet.MeetShort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeetShortRepository extends JpaRepository<MeetShort, Long> {

    List<MeetShort> findByUserIdx(Long userIdx);

    List<MeetShort> findByTitleContaining(String keyword);
}
