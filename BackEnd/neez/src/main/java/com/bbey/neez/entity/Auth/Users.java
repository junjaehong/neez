package com.bbey.neez.entity.Auth;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(name = "userId", nullable = false, unique = true, length = 255)
    private String userId;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(length = 255)
    private String phone;

    @Column(length = 255)
    private String address;

    @Column(nullable = false)
    private boolean verified;

    @Column(length = 500)
    private String refreshToken;

    @Column(length = 50)
    private String resetCode;

    private LocalDateTime resetCodeExpire;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 회사 연결
    @Column(name = "company_idx")
    private Long companyIdx;

    @Column(name = "card_company_name", length = 255)
    private String cardCompanyName;

    @Column(length = 100)
    private String department;

    @Column(length = 100)
    private String position;

    @Column(length = 50)
    private String fax;

    // 🔥 역할 컬럼
    @Column(name = "role", nullable = false, length = 50)
    private String role;   // "USER", "ADMIN" 등
}
