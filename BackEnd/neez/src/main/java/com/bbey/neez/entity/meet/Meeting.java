package com.bbey.neez.entity.meet;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "meetings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Meeting {

    // PK
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    // 회의 생성자 (users.idx)
    @Column(name = "user_idx", nullable = false)
    private Long userIdx;

    // 회의 제목
    @Column(length = 200, nullable = false)
    private String title;

    // 회의 설명 (있다면)
    @Column(columnDefinition = "TEXT")
    private String description;

    // 회의 시작 시간
    @Column(name = "started_at")
    private LocalDateTime startedAt;

    // 회의 종료 시간
    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    // 상태 (예: ONGOING / FINISHED 등)
    @Column(length = 20)
    private String status;

    // 기본 언어 (예: ko, en)
    @Column(name = "main_lang", length = 10)
    private String mainLang;

    // 전체 녹음 파일 URL (m4a 등)
    @Column(name = "audio_url", length = 500)
    private String audioUrl;

    // 생성 시각
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // 수정 시각
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
