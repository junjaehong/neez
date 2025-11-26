package com.bbey.neez.repository.meet;

import com.bbey.neez.entity.meet.MeetRTChunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeetRTChunkRepository extends JpaRepository<MeetRTChunk, Long> {

    // 회의별 전체 조각을 순서대로
    List<MeetRTChunk> findByMeetIdxOrderBySeqAsc(Long meetIdx);
}
