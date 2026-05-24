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
    public String createToken(String username) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                .setSubject(username) // 토큰 주인
                .setIssuedAt(now)     // 발행 시간
                .setExpiration(expiryDate) // 만료 시간
                .signWith(key)        // 서명
                .compact();
    }

    // 토큰에서 아이디 추출
    public String getUsername(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }
}