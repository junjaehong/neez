package com.bbey.neez.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    // 만료 시간 (ms) – 기본값: 1시간 / 7일
    private final long accessExpMs;
    private final long refreshExpMs;

    // 서명용 SecretKey
    private final SecretKey secretKey;

    /**
     * 생성자에서 설정값 주입
     *
     * application.yml 에서:
     * jwt.secret: "길이 32바이트 이상 랜덤 문자열"
     * jwt.access-exp-ms: 3600000
     * jwt.refresh-exp-ms: 604800000
     */
    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-exp-ms:3600000}") long accessExpMs,
            @Value("${jwt.refresh-exp-ms:604800000}") long refreshExpMs
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpMs = accessExpMs;
        this.refreshExpMs = refreshExpMs;
    }

    // -----------------------------
    // Access Token 생성
    // -----------------------------
    public String createAccessToken(String userId) {
        return createToken(userId, accessExpMs);
    }

    // -----------------------------
    // Refresh Token 생성
    // -----------------------------
    public String createRefreshToken(String userId) {
        return createToken(userId, refreshExpMs);
    }

    // 공통 토큰 생성 로직
    private String createToken(String userId, long expireMs) {
        long now = System.currentTimeMillis();

        return Jwts.builder()
                .setSubject(userId)                                 // userId 저장
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expireMs))           // 만료 시간
                .signWith(secretKey, SignatureAlgorithm.HS256)      // HS256 + 고정 secretKey
                .compact();
    }

    // -----------------------------
    // 토큰에서 userId 추출
    // -----------------------------
    public String getUserIdFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            // 서명 불일치, 만료, 형식 오류 등 → null
            return null;
        }
    }

    // -----------------------------
    // 토큰 만료 여부 체크
    // -----------------------------
    public boolean isExpired(String token) {
        try {
            Date exp = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();

            return exp.before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            // 파싱 실패도 만료된 것으로 간주
            return true;
        }
    }
}
