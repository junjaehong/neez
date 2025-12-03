package com.bbey.neez.entity.Meet;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "meetShorts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetShort {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(name = "user_idx", nullable = false)
    private Long userIdx;     // 만든 사람 (또는 회의 생성자)

    @Column(nullable = false, length = 200)
    private String title;     // 회의명 (meetings.title 복사해서 저장)

    @Column(name = "shorts", columnDefinition = "TEXT")
    private String shorts;    // 요약 내용 (bullet list)

    @Column(name = "summary_lang", length = 10)
    private String summaryLang;  // ko 등

    @Column(name = "audio_url", length = 500)
    private String audioUrl;  // 전체 녹음 파일 URL

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false)
    private LocalDateTime updatedAt;
}
