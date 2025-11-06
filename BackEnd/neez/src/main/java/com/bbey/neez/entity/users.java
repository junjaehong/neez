package com.bbey.neez.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;   // PK (팀 규칙에 맞게 idx 사용)

    @Column(nullable = false, unique = true)
    private String userId;  // 로그인용 아이디

    @Column(nullable = false)
    private String password; // 비밀번호

    @Column(nullable = false)
    private String name; // 이름 — 아이디 찾기용

    @Column(nullable = false)
    private String email; // 이메일 — 아이디/비밀번호 찾기용

    @Column(nullable = false)
    private LocalDateTime created_at = LocalDateTime.now();

    @Column(nullable = false)
    private LocalDateTime updated_at = LocalDateTime.now();
    
}
