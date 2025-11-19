package com.bbey.neez.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "companies")
@Getter
@Setter
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;          // PK

    @Column(name = "name", nullable = false)
    private String name;       // 회사명

    @Column(name = "rep_name")
    private String repName;    // 대표이름

    @Column(name = "biz_no", unique = true)
    private String bizNo;      // 사업자등록번호

    @Column(name = "corp_no", unique = true)
    private String corpNo;     // 법인등록번호

    @Column(name = "address")
    private String address;    // 주소

    @Column(name = "homepage")
    private String homepage;   // 홈페이지

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
