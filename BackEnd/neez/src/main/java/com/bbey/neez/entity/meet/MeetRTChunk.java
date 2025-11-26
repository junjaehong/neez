package com.bbey.neez.entity.meet;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "meetRTChunks")
@Getter
@Setter
public class MeetRTChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(name = "meet_idx", nullable = false)
    private Long meetIdx;  // 나중에 @ManyToOne( Meeting ) 으로 바꿔도 됨

    @Column(name = "seq", nullable = false)
    private Long seq;

    @Column(name = "chunk_type", nullable = false, length = 16)
    private String chunkType;   // 예: "partial", "final"

    @Column(name = "lang_code", length = 10)
    private String langCode;    // 예: "ko", "en"

    @Lob
    @Column(name = "content", nullable = false)
    private String content;     // STT 결과 텍스트

    @Column(name = "is_final", nullable = false)
    private boolean finalChunk; // tinyint(1) ↔ boolean

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;
}
