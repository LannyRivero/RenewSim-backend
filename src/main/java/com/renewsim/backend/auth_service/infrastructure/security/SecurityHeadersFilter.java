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

    private static final String CSP = String.join(" ",
            "default-src 'none';",
            "connect-src 'self';",
            "frame-ancestors 'none';",
            "base-uri 'none';",
            "form-action 'none';",
            "object-src 'none';",
            "block-all-mixed-content");

    private static final String HSTS = "max-age=31536000; includeSubDomains";
    private static final String PERMISSIONS_POLICY = "geolocation=(), microphone=(), camera=()";

    private static final String CORP = "same-origin";
    private static final String COOP = "same-origin";
    private static final String XPCDP = "none";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws ServletException, IOException {

        setIfAbsent(response, "X-Content-Type-Options", "nosniff");
        setIfAbsent(response, "X-Frame-Options", "DENY");
        setIfAbsent(response, "Referrer-Policy", "no-referrer");
        setIfAbsent(response, "Content-Security-Policy", CSP);
        setIfAbsent(response, "Permissions-Policy", PERMISSIONS_POLICY);

        setIfAbsent(response, "Cross-Origin-Resource-Policy", CORP);
        setIfAbsent(response, "Cross-Origin-Opener-Policy", COOP);
        setIfAbsent(response, "X-Permitted-Cross-Domain-Policies", XPCDP);
        setIfAbsent(response, "Permissions-Policy", "geolocation=(), microphone=(), camera=(), fullscreen=()");


        boolean https = request.isSecure()
                || "https".equalsIgnoreCase(request.getHeader("X-Forwarded-Proto"));
        if (https) {
            setIfAbsent(response, "Strict-Transport-Security", HSTS);
        }

        chain.doFilter(request, response);
    }

    private void setIfAbsent(HttpServletResponse res, String name, String value) {
        if (!res.containsHeader(name)) {
            res.setHeader(name, value);
        }
    }
}
