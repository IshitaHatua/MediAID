package com.cts.gateway.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;
    private static final String SECRET = "your-very-secure-secret-key-that-is-at-least-32-characters-long";

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
    }

    private String generateToken(String subject, String role, long userId, long expiryMillis) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject(subject)
                .claim("role", role)
                .claim("userId", userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiryMillis))
                .signWith(key)
                .compact();
    }

    @Test
    void validateJwtToken_returnsTrueForValidToken() {
        String token = generateToken("test@example.com", "CITIZEN", 1L, 60_000);
        assertTrue(jwtUtils.validateJwtToken(token));
    }

    @Test
    void validateJwtToken_returnsFalseForExpiredToken() {
        String token = generateToken("test@example.com", "CITIZEN", 1L, -1000);
        assertFalse(jwtUtils.validateJwtToken(token));
    }

    @Test
    void validateJwtToken_returnsFalseForBogusString() {
        assertFalse(jwtUtils.validateJwtToken("not.a.token"));
        assertFalse(jwtUtils.validateJwtToken("xxx"));
    }

    @Test
    void getUsernameFromToken_returnsSubject() {
        String token = generateToken("alice@example.com", "ADMIN", 42L, 60_000);
        assertEquals("alice@example.com", jwtUtils.getUsernameFromToken(token));
    }

    @Test
    void getRoleFromToken_returnsRoleClaim() {
        String token = generateToken("alice@example.com", "OFFICER", 42L, 60_000);
        assertEquals("OFFICER", jwtUtils.getRoleFromToken(token));
    }

    @Test
    void getUserIdFromToken_returnsUserIdAsLong() {
        String token = generateToken("alice@example.com", "OFFICER", 42L, 60_000);
        assertEquals(42L, jwtUtils.getUserIdFromToken(token));
    }
}
