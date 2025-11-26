package com.bbey.neez.repository.meet;

import com.bbey.neez.entity.meet.MeetShort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeetShortRepository extends JpaRepository<MeetShort, Long> {

    List<MeetShort> findByUserIdxOrderByCreatedAtDesc(Long userIdx);

    // 필요하면 meet_idx 컬럼 추가해서 회의 기준으로도 조회 가능
}
