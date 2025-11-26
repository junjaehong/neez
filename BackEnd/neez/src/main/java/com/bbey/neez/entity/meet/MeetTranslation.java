package com.bbey.neez.entity.meet;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "meetTranslations")
@Getter
@Setter
public class MeetTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(name = "meet_idx", nullable = false)
    private Long meetIdx;     // 회의 ID

    @Column(name = "lang_code", nullable = false, length = 10)
    private String langCode;  // 번역 언어 코드 (예: "en")

    @Lob
    @Column(name = "translated")
    private String translated; // 번역된 전체 텍스트 or 조각

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false)
    private LocalDateTime updatedAt;
}
