package com.cts.user.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Authentication filter for downstream services.
 * Authentication is centralised at the API Gateway. This filter reads the
 * trusted user-identity headers injected by the gateway and populates the SecurityContext.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String usernameHeader = request.getHeader("X-Username");
        String roleHeader = request.getHeader("X-User-Role");

        if (usernameHeader != null && roleHeader != null) {
            try {
                String authority = roleHeader.startsWith("ROLE_") ? roleHeader : "ROLE_" + roleHeader;
                var authorities = List.of(new SimpleGrantedAuthority(authority));
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(usernameHeader, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                // Invalid header format — leave SecurityContext unauthenticated
            }
        }

        filterChain.doFilter(request, response);
    }
}
