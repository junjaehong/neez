package com.bbey.neez.entity.meet;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "meetShorts")
@Getter
@Setter
public class MeetShort {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(name = "user_idx", nullable = false)
    private Long userIdx;      // users.idx (나중에 @ManyToOne User 로 바꿔도 ok)

    @Column(name = "title", nullable = false, length = 200)
    private String title;      // 회의명 (예: 251126-고객미팅)

    @Lob
    @Column(name = "shorts")
    private String shorts;     // 요약본

    @Column(name = "summary_lang", length = 10)
    private String summaryLang; // "ko", "en" 등

    @Column(name = "audio_url", length = 500)
    private String audioUrl;    // 원본 녹음 파일 위치

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false)
    private LocalDateTime updatedAt;
}
