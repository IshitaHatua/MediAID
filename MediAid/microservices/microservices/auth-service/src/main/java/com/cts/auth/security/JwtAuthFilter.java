package com.cts.auth.security;

import com.cts.auth.util.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Auth-service is responsible for issuing JWTs. To allow it to also serve a
 * few authenticated admin endpoints (e.g. GET /api/auth/users), this filter
 * also accepts and validates Bearer tokens. When the gateway has already
 * injected X-Username / X-User-Role headers (most production traffic), they
 * take precedence and the JWT is just validated as a defence-in-depth step.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final JwtUtils jwtUtils;

    public JwtAuthFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1) Prefer trusted headers injected by the gateway.
        String usernameHeader = request.getHeader("X-Username");
        String roleHeader = request.getHeader("X-User-Role");
        if (usernameHeader != null && roleHeader != null
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                String authority = roleHeader.startsWith("ROLE_") ? roleHeader : "ROLE_" + roleHeader;
                var authorities = List.of(new SimpleGrantedAuthority(authority));
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(usernameHeader, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                log.debug("Failed to apply gateway headers: {}", e.getMessage());
            }
        }

        // 2) Fallback: validate the JWT directly (for direct calls or tests).
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = authHeader.substring(7);
            try {
                if (jwtUtils.validateJwtToken(token)) {
                    String username = jwtUtils.getUserNameFromJwtToken(token);
                    String role = jwtUtils.getRoleFromJwtToken(token);
                    if (username != null && role != null) {
                        String authority = role.startsWith("ROLE_") ? role : "ROLE_" + role;
                        var authorities = List.of(new SimpleGrantedAuthority(authority));
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(username, null, authorities);
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            } catch (Exception e) {
                log.warn("JWT validation failed: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}
