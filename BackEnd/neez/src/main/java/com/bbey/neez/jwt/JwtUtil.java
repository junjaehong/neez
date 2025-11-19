package com.bbey.neez.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    // Access Token: 1시간
    private final long ACCESS_EXP = 1000L * 60 * 60;

    // Refresh Token: 7일
    private final long REFRESH_EXP = 1000L * 60 * 60 * 24 * 7;

    // JWT 서명용 SecretKey 자동 생성
    private final SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // Access Token 발급
    public String createAccessToken(String userId) {
        return createToken(userId, ACCESS_EXP);
    }

    // Refresh Token 발급
    public String createRefreshToken(String userId) {
        return createToken(userId, REFRESH_EXP);
    }

    // 공통 토큰 생성 로직
    private String createToken(String userId, long expireTime) {
        return Jwts.builder()
                .setSubject(userId)                     // 토큰에 userId 넣기
                .setIssuedAt(new Date())                // 발급 시간
                .setExpiration(new Date(System.currentTimeMillis() + expireTime)) // 만료 시간
                .signWith(secretKey)                    // 서명
                .compact();
    }

    // 토큰 → userId 추출
    public String getUserId(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (Exception e) {
            return null;
        }
    }

    // 토큰 만료 여부 확인
    public boolean isExpired(String token) {
        try {
            Date expiration = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();

            return expiration.before(new Date());
        } catch (Exception e) {
            return true; // 에러 나도 만료된 것으로 처리
        }
    }
}
