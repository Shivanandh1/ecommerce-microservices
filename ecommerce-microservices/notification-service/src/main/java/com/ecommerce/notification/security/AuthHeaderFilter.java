package com.ecommerce.notification.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class AuthHeaderFilter extends OncePerRequestFilter {

    private static final String HEADER_USER = "X-Auth-User";
    private static final String HEADER_ROLE = "X-Auth-Role";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (isPublic(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String user = request.getHeader(HEADER_USER);
        if (user == null || user.isBlank()) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        String role = request.getHeader(HEADER_ROLE);
        SimpleGrantedAuthority authority = role != null ? new SimpleGrantedAuthority(role) : new SimpleGrantedAuthority("ROLE_USER");

        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(user, null, Collections.singletonList(authority));
        SecurityContextHolder.getContext().setAuthentication(auth);

        filterChain.doFilter(request, response);
    }

    private boolean isPublic(String path) {
        return path.startsWith("/api/auth") || path.startsWith("/actuator");
    }
}
