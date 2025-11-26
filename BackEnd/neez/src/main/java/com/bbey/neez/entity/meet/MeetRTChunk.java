package com.bbey.neez.entity.Meet;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "meetRTChunks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeetRTChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(name = "meet_idx", nullable = false)
    private Long meetIdx;       // meetings.idx

    @Column(nullable = false)
    private Long seq;           // chunk 순서

    @Column(name = "chunk_type", length = 16, nullable = false)
    private String chunkType;   // STT / USER_NOTE 등 구분용 (안쓰면 STT 고정)

    @Column(name = "lang_code", length = 10)
    private String langCode;    // ko, en 등

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;     // 인식된 텍스트

    @Column(name = "is_final", nullable = false)
    private boolean finalChunk; // tinyint(1) -> boolean

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;
}
