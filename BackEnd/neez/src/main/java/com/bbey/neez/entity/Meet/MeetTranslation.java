package com.bbey.neez.entity.Meet;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "meetTranslations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetTranslation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(name = "meet_idx", nullable = false)
    private Long meetIdx;

    @Column(name = "lang_code", length = 10, nullable = false)
    private String langCode;    // 번역 언어

    @Column(columnDefinition = "TEXT")
    private String translated;  // 번역 결과 전체

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false)
    private LocalDateTime updatedAt;
}
