package com.example.chapter6.jwt;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.Instant;
import java.util.stream.DoubleStream;

@Component
public class JwtTokenProvider {

    // 시크릿키
    private final String jwtSecret;
    // 토큰만료일
    private final long jwtExpirationMs;
    private DoubleStream Jwts;

    public JwtTokenProvider(
            @Value("${app.jwt.secret}") String jwtSecret,
            @Value("${app.jwt.expiration}") long jwtExpirationMs
    ) {
        this.jwtSecret = jwtSecret;
        this.jwtExpirationMs = jwtExpirationMs;
    }

    //토큰 생성
    public String generateToken(String userId){

        // 현재 시간 + 만료 시간(ms)
        Instant expireDate = Instant.now().plusMillis(jwtExpirationMs);

        return io.jsonwebtoken.Jwts.builder()
                .setSubject(userId)
                .setIssuedAt(Date.from(Instant.now()))
                .setExpiration(java.util.Date.from(expireDate))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    // 토큰으로 사용자 아이디 반환
    public String getUserIdFromJWT(String token) {
        Claims claims = io.jsonwebtoken.Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    //토큰으로 만료일 반환
    public java.util.Date getTokenExpiryFromJWT(String token){
        Claims claims = io.jsonwebtoken.Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody();
        return claims.getExpiration();
    }

}
