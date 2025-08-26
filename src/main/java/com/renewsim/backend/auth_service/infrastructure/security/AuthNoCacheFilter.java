package com.renewsim.backend.auth_service.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class AuthNoCacheFilter extends OncePerRequestFilter {

    private static final String AUTH_PATTERN = "/api/v1/auth/**";
    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws ServletException, IOException {
        try {
            chain.doFilter(request, response);
        } finally {
            final String path = request.getRequestURI();
            if (MATCHER.match(AUTH_PATTERN, path)) {
                response.setHeader("Cache-Control", "no-store, max-age=0");
                response.setHeader("Pragma", "no-cache");
                response.setHeader("Expires", "0");
            }
        }
    }
}
