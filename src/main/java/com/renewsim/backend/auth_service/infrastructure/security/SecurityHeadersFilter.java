package com.renewsim.backend.auth_service.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityHeadersFilter extends OncePerRequestFilter {

    private static final String CSP =
            "default-src 'none'; " +
            "connect-src 'self'; " +
            "frame-ancestors 'none'; " +
            "base-uri 'none'; " +
            "object-src 'none'; " +
            "block-all-mixed-content";

    private static final String HSTS = "max-age=31536000; includeSubDomains";

    private static final String PERMISSIONS_POLICY =
            "geolocation=(), microphone=(), camera=()";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("Referrer-Policy", "no-referrer");
        response.setHeader("Content-Security-Policy", CSP);
        response.setHeader("Permissions-Policy", PERMISSIONS_POLICY);

        boolean https =
                request.isSecure()
                || "https".equalsIgnoreCase(request.getHeader("X-Forwarded-Proto"));
        if (https) {
            response.setHeader("Strict-Transport-Security", HSTS);
        }

        chain.doFilter(request, response);
    }
}


