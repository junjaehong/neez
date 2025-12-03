package com.bbey.neez.entity.Meet;

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

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;           // meetings.idx

    @Column(name = "user_idx", nullable = false)
    private Long userIdx;       // 생성한 사용자 (users.idx)

    @Column(nullable = false, length = 200)
    private String title;       // 회의 제목

    @Column(columnDefinition = "TEXT")
    private String description; // 선택적 회의 설명 (없으면 지워도 됨)

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(length = 20)
    private String status;      // ONGOING / FINISHED 등 (없으면 지워도 됨)

    @Column(name = "main_lang", length = 10)
    private String mainLang;    // 기본 언어 (ko / en 등)

    @Column(name = "audio_url", length = 500)
    private String audioUrl;    // 전체 녹음 파일 URL

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false)
    private LocalDateTime updatedAt;
}
