package com.bbey.neez.repository.Meet;

import com.bbey.neez.entity.Meet.MeetTranslation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeetTranslationRepository extends JpaRepository<MeetTranslation, Long> {

    List<MeetTranslation> findByMeetIdx(Long meetIdx);

    MeetTranslation findByMeetIdxAndLangCode(Long meetIdx, String langCode);
}
