package com.example.taskmanager.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expirationMinutes}")
    private long expirationMinutes;

    // ========== Public API ==========

    /** Create a JWT with the given subject (usually the user's email) and optional claims (like role). */
    public String generateToken(String subject, Map<String, Object> extraClaims) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(expirationMinutes * 60);

        return Jwts.builder()
                .setClaims(extraClaims)               // 0.11.x uses setClaims(...)
                .setSubject(subject)                  // 0.11.x uses setSubject(...)
                .setIssuedAt(Date.from(now))          // 0.11.x uses setIssuedAt(...)
                .setExpiration(Date.from(expiry))     // 0.11.x uses setExpiration(...)
                .signWith(getSignKey(), SignatureAlgorithm.HS256) // 0.11.x requires algo here
                .compact();
    }

    /** Extract the subject (email/username) from a token. */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /** Validate token belongs to this subject (email) and is not expired/tampered. */
    public boolean isTokenValid(String token, String expectedSubject) {
        final String username = extractUsername(token);
        return username.equals(expectedSubject) && !isTokenExpired(token);
    }

    // ========== Helpers ==========

    private boolean isTokenExpired(String token) {
        Date exp = extractClaim(token, Claims::getExpiration);
        return exp.before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        final Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        // 0.11.x parser API:
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Create the signing key. Supports either:
     * - A long random ASCII string (>= 64 chars), or
     * - A Base64-encoded key (prefix with "BASE64:" in application.yml if you want that later)
     */
    private Key getSignKey() {
        if (secret.startsWith("BASE64:")) {
            byte[] keyBytes = Decoders.BASE64.decode(secret.substring("BASE64:".length()));
            return Keys.hmacShaKeyFor(keyBytes);
        }
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes); // HS256 requires >= 32 bytes; use 64+ chars
    }
}
