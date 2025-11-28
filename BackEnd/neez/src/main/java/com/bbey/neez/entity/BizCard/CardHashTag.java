package com.bbey.neez.entity.BizCard;

import javax.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "cardHashTags",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_card_tag", columnNames = {"card_idx", "tag_idx"})
        }
)
public class CardHashTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    // 명함 FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_idx", nullable = false)
    private BizCard card;

    // 태그 FK
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_idx", nullable = false)
    private HashTag tag;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getIdx() {
        return idx;
    }

    public void setIdx(Long idx) {
        this.idx = idx;
    }

    public BizCard getCard() {
        return card;
    }

    public void setCard(BizCard card) {
        this.card = card;
    }

    public HashTag getTag() {
        return tag;
    }

    public void setTag(HashTag tag) {
        this.tag = tag;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
