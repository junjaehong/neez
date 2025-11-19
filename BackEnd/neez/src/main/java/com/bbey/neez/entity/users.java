package com.bbey.neez.entity;

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
    private Long id;            // PK (자동 증가)

    @Column(nullable = false, unique = true)
    private String userId;      // 로그인 ID

    @Column(nullable = false)
    private String password;    // 암호화된 비밀번호

    @Column(nullable = false)
    private String name;        // 이름

    @Column(nullable = false, unique = true)
    private String email;       // 이메일

    private String phone;       // 전화번호

    private boolean verified;   // 이메일 인증 여부

    private LocalDateTime created_at;
    private LocalDateTime updated_at;

    @Column(length = 500)
    private String refreshToken;  // Refresh Token 저장

    // BizCardReaderServiceImpl 에서 필요함
    public Long getIdx() {
        return this.id;  // id를 idx처럼 반환
    }
}
