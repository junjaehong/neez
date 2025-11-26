package com.bbey.neez.repository.meet;

import com.bbey.neez.entity.meet.MeetTranslation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeetTranslationRepository extends JpaRepository<MeetTranslation, Long> {

    List<MeetTranslation> findByMeetIdxAndLangCodeOrderByCreatedAtAsc(Long meetIdx, String langCode);
}
