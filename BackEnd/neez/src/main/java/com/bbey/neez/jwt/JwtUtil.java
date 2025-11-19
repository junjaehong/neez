package com.bbey.neez.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    private final long ACCESS_EXP = 1000L * 60 * 60;        // 1시간
    private final long REFRESH_EXP = 1000L * 60 * 60 * 24 * 7; // 7일

    private final SecretKey secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public String createAccessToken(String userId) {
        return createToken(userId, ACCESS_EXP);
    }

    public String createRefreshToken(String userId) {
        return createToken(userId, REFRESH_EXP);
    }

    private String createToken(String userId, long expire) {
        return Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expire))
                .signWith(secretKey)
                .compact();
    }

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

    public boolean isExpired(String token) {
        try {
            Date exp = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();

            return exp.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}
