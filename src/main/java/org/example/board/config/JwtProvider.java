package org.example.board.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtProvider {
    // 보안을 위한 비밀키
    private final SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    // 토큰 유효 기간 (1시간)
    private final long expirationTime = 3600000;

    // 토큰 생성
    public String createToken(String username, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .setSubject(username) // 토큰 주인
                .claim("role", role) // 권한
                .setIssuedAt(now)     // 발행 시간
                .setExpiration(expiryDate) // 만료 시간
                .signWith(key)        // 서명
                .compact();
    }

    // 토큰에서 아이디 추출
    public String getUsername(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    // 토큰에서 권한 추출
    public String getRole(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("role", String.class);
    }
}