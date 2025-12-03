package com.bbey.neez.repository.Meet;

import com.bbey.neez.entity.Meet.MeetRTChunk;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeetRTChunkRepository extends JpaRepository<MeetRTChunk, Long> {

    List<MeetRTChunk> findByMeetIdxOrderBySeqAsc(Long meetIdx);
}
