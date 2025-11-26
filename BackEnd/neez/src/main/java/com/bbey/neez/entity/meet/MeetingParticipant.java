package com.bbey.neez.entity.Meet;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "meetingParticipants")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetingParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(name = "meet_idx", nullable = false)
    private Long meetIdx;       // meetings.idx

    @Column(name = "bizcard_idx", nullable = false)
    private Long bizcardIdx;    // bizCards.idx

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;
}
