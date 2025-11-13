package com.bbey.neez.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(
        name = "companies",
        indexes = {
                @Index(name = "idx_companies_name", columnList = "name"),
                @Index(name = "idx_companies_homepage", columnList = "homepage")
        }
)
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(nullable = false, length = 255)
    private String name;          // 회사명

    @Column(name = "rep_name", length = 100)
    private String repName;       // 대표이름

    @Column(name = "biz_no", length = 20, unique = true)
    private String bizNo;         // 사업자등록번호 (숫자만, unique)

    @Column(name = "corp_no", length = 20, unique = true)
    private String corpNo;        // 법인등록번호 (숫자만, unique)

    @Column(length = 255)
    private String address;       // 주소

    @Column(length = 255)
    private String homepage;      // 홈페이지 URL

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
