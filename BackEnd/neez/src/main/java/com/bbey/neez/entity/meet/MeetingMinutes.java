package com.bbey.neez.entity.Meet;

import com.bbey.neez.entity.BizCard.BizCard;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "meeting_minutes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingMinutes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    // 어떤 회의의 회의록인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meeting_id", nullable = false)
    private Meeting meeting;

    // (옵션) 어떤 명함과 연결된 회의록인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bizcard_id")
    private BizCard bizCard;

    // 요약본
    @Column(name = "summary_text", columnDefinition = "TEXT")
    private String summaryText;

    // full transcript
    @Column(name = "minutes_text", columnDefinition = "TEXT", nullable = false)
    private String minutesText;

    // 혹시 파일로도 저장할 거면 파일명
    @Column(name = "file_name",length = 255)
    private String fileName;

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false)
    private LocalDateTime updatedAt;
}
