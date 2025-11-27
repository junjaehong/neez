package com.bbey.neez.service.Meet;

import com.bbey.neez.entity.Meet.MeetTranslation;
import com.bbey.neez.repository.Meet.MeetTranslationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MeetingTranslationService {

    private final MeetTranslationRepository translationRepository;

    /**
     * 특정 회의 + 언어 코드에 대해
     * 번역된 텍스트를 청크 단위로 계속 이어 붙인다.
     */
    public void appendTranslated(Long meetIdx,
            String langCode,
            String translatedChunk) {

        MeetTranslation translation = translationRepository.findByMeetIdxAndLangCode(meetIdx, langCode);

        if (translation == null) {
            translation = MeetTranslation.builder()
                    .meetIdx(meetIdx)
                    .langCode(langCode)
                    .translated(translatedChunk)
                    .build();
        } else {
            String old = translation.getTranslated();
            if (old == null || old.isEmpty()) {
                translation.setTranslated(translatedChunk);
            } else {
                translation.setTranslated(old + "\n" + translatedChunk);
            }
        }

        translationRepository.save(translation);
    }

    /**
     * 전체 번역본 조회
     */
    @Transactional(readOnly = true)
    public String getFullTranslation(Long meetIdx, String langCode) {
        MeetTranslation translation = translationRepository.findByMeetIdxAndLangCode(meetIdx, langCode);

        if (translation == null || translation.getTranslated() == null) {
            return "";
        }
        return translation.getTranslated();
    }
}
