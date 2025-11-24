package com.bbey.neez.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "email_verification_token")
@Getter
@Setter
public class EmailVerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String token;          // 랜덤 토큰 (UUID 등)

    @Column(nullable = false, length = 255)
    private String userId;

    @Column(nullable = false, length = 255)
    private String password;      // 이미 BCrypt로 인코딩된 상태

    @Column(length = 100)
    private String name;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(length = 255)
    private String phone;

    @Column(nullable = false)
    private LocalDateTime expiresAt; // 만료 시간 (예: now + 30분)

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;


    // ================================
    // 회사 관련 필드
    // ================================

    @Column(name = "card_company_name", length = 255)
    private String cardCompanyName;    // 명함용 회사명

    @Column(name = "company_idx")
    private Long companyIdx;           // companies.idx

    @Column(length = 100)
    private String department;         // 부서

    @Column(length = 100)
    private String position;           // 직급

    @Column(length = 50)
    private String fax;                // 팩스번호
}
