package com.cts.auth.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
    }

    @Test
    void generateToken_producesNonEmptyToken() {
        String token = jwtUtils.generateToken("alice@example.com", "CITIZEN", 42L);

        assertNotNull(token);
        assertFalse(token.isBlank());
        // JWT has three Base64Url-encoded sections joined by '.'
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    void validateJwtToken_returnsTrueForFreshToken() {
        String token = jwtUtils.generateToken("alice@example.com", "ADMIN", 1L);
        assertTrue(jwtUtils.validateJwtToken(token));
    }

    @Test
    void validateJwtToken_returnsFalseForMalformedToken() {
        assertFalse(jwtUtils.validateJwtToken("this.is.not.a.valid.token"));
        assertFalse(jwtUtils.validateJwtToken("totally-junk"));
    }

    @Test
    void getUserNameFromJwtToken_returnsSubject() {
        String token = jwtUtils.generateToken("bob@example.com", "OFFICER", 7L);
        assertEquals("bob@example.com", jwtUtils.getUserNameFromJwtToken(token));
    }

    @Test
    void getRoleFromJwtToken_returnsRoleClaim() {
        String token = jwtUtils.generateToken("bob@example.com", "MANAGER", 7L);
        assertEquals("MANAGER", jwtUtils.getRoleFromJwtToken(token));
    }
}
