package com.bbey.neez.entity.meet;

import com.bbey.neez.entity.BizCard;
import lombok.*;
import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "meetingParticipants")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meet_idx", nullable = false)
    private Meeting meeting;  // 회의 엔티티 (이미 존재해야 함)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bizcard_idx", nullable = false)
    private BizCard bizCard;  // 명함 엔티티 (bizCards 테이블 매핑)

    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
