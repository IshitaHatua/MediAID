package com.cts.compliance.security;

import com.cts.compliance.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserUtil {

    private final HttpServletRequest request;
    private final JwtUtils jwtUtils;

    public CurrentUserUtil(HttpServletRequest request, JwtUtils jwtUtils) {
        this.request  = request;
        this.jwtUtils = jwtUtils;
    }

    public Long getUserId() {
        String userIdHeader = request.getHeader("X-User-Id");
        if (userIdHeader != null) return Long.parseLong(userIdHeader);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Long) {
            return (Long) authentication.getPrincipal();
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtils.validateJwtToken(token)) return jwtUtils.getUserIdFromJwtToken(token);
        }

        throw new RuntimeException("No authenticated user found");
    }
}
