package com.bbey.neez.entity;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "companies")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "domain")
    private String domain;

    @Column(name = "industry")
    private String industry;

    // AI가 뽑아온 요약/설명
    @Column(name = "detail", columnDefinition = "TEXT")
    private String detail;

    @Column(name = "source")
    private String source;

    // 0~1 신뢰도
    @Column(name = "confidence", precision = 3, scale = 2)
    private BigDecimal confidence;

    @Column(name = "last_refreshed_at")
    private LocalDateTime last_refreshed_at;

    @Column(name = "created_at")
    private LocalDateTime created_at;

    @Column(name = "updated_at")
    private LocalDateTime updated_at;
}
