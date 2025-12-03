package com.bbey.neez.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "companies")
@Getter
@Setter
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "rep_name", length = 100)
    private String repName;

    @Column(name = "biz_no", length = 20, unique = true)
    private String bizNo;

    @Column(name = "corp_no", length = 20, unique = true)
    private String corpNo;

    @Column(name = "dart_corp_code", length = 20)
    private String dartCorpCode;

    @Column(length = 255)
    private String address;

    @Column(length = 255)
    private String homepage;

    @Column(precision = 19, scale = 2)
    private BigDecimal confidence;

    @Column(length = 255)
    private String domain;

    @Column(length = 255)
    private String industry;

    @Column(name = "last_refreshed_at")
    private LocalDateTime lastRefreshedAt;

    @Column(length = 255)
    private String source;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
