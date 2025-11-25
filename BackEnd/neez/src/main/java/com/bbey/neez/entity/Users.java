package com.bbey.neez.entity;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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

    @Column(nullable = false, unique = true)
    private String userId;

    @Column(nullable = false)
    private String password;

    @Column(length = 100)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;

    @Column(nullable = false)
    private boolean verified;   // A안에서는 항상 true로 저장해도 됨

    private String refreshToken;

    private String resetCode;
    private LocalDateTime resetCodeExpire;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

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
