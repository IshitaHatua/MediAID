package com.cts.util;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtils {
	
    private final String jwtSecret = "your-very-secure-secret-key-that-is-at-least-32-characters-long";
    
    private SecretKey getSigningKey() {
    	return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    	
    }
    
    public Long getUserIdFromJwtToken(String token) {
    	
    	return Jwts.parser()
    			.verifyWith(getSigningKey())
    			.build()
    			.parseSignedClaims(token)
    			.getPayload()
    			.get("userId",Long.class);

    }
    
    public boolean validateJwtToken(String token) {
    	try {
    		Jwts.parser()
    			.verifyWith(getSigningKey())
    			.build()
    			.parseSignedClaims(token);
    		return true;
    	}
    	catch(JwtException ex) {
    		return false;
    	}
    }
    
    public String getRoleFromJwtToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class);
    }

}
