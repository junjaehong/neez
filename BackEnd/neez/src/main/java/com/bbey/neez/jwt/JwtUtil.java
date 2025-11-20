package com.bbey.neez.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    private final long ACCESS_EXP = 1000L * 60 * 60;          // 1시간
    private final long REFRESH_EXP = 1000L * 60 * 60 * 24 * 7; // 7일

    // HS256용 SecretKey 생성
    private final SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // -----------------------------
    // Access Token 생성
    // -----------------------------
    public String createAccessToken(String userId) {
        return createToken(userId, ACCESS_EXP);
    }

    // -----------------------------
    // Refresh Token 생성
    // -----------------------------
    public String createRefreshToken(String userId) {
        return createToken(userId, REFRESH_EXP);
    }

    // 토큰 생성 공통 로직
    private String createToken(String userId, long expire) {
        return Jwts.builder()
                .setSubject(userId)                     // userId 저장
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expire))
                .signWith(secretKey)
                .compact();
    }

    // -----------------------------
    // 토큰에서 userId 추출 (refresh(), security 전체에서 공용)
    // -----------------------------
    public String getUserIdFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)           // 서명 검증
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();                     // subject = userId

        } catch (JwtException | IllegalArgumentException e) {
            return null; // 잘못된 토큰일 경우
        }
    }

    // 기존 getUserId()와 동일 기능, 유지해도 됨
    public String getUserId(String token) {
        return getUserIdFromToken(token);
    }

    // -----------------------------
    // 만료 여부 체크
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
            return true;   // 파싱실패 → 만료된 것으로 간주
        }
    }
}
