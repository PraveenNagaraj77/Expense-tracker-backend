package com.praveen.expensetracker.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}") // e.g., 86400000 for 1 day
    private long jwtExpirationMs;

    // ✅ Generate token
    public String generateToken(String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // ✅ Extract email (subject)
    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    // ✅ Validate token (basic: only checks structure & signature)
    public boolean validateToken(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // ✅ Validate token with user check (Overloaded Method)
    public boolean validateToken(String token, UserDetails userDetails) {
        final String email = extractEmail(token);
        return (email.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    // ✅ Check if token is expired
    private boolean isTokenExpired(String token) {
        Date expiration = extractClaims(token).getExpiration();
        return expiration.before(new Date());
    }

    // ✅ Internal: Extract all claims
    private Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // ✅ Internal: Create signing key
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
}
