package com.renewsim.backend.auth_service.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityHeadersFilter extends OncePerRequestFilter {

    // CSP estricta (para endpoints normales)
    private static final String CSP = String.join(" ",
            "default-src 'none';",
            "connect-src 'self';",
            "frame-ancestors 'none';",
            "base-uri 'none';",
            "form-action 'none';",
            "object-src 'none';",
            "block-all-mixed-content");

    // CSP relajada (para Swagger en prod)
    private static final String SWAGGER_CSP = String.join(" ",
            "default-src 'self';",
            "script-src 'self' 'unsafe-inline';",
            "style-src 'self' 'unsafe-inline';",
            "img-src 'self' data:;",
            "connect-src 'self';",
            "frame-ancestors 'none';",
            "object-src 'none';");

    private static final String HSTS = "max-age=31536000; includeSubDomains";
    private static final String PERMISSIONS_POLICY = "geolocation=(), microphone=(), camera=(), fullscreen=()";
    private static final String CORP = "same-origin";
    private static final String COOP = "same-origin";
    private static final String XPCDP = "none";

    // Lee el perfil activo de Spring
    @Value("${spring.profiles.active:dev}")
    private String activeProfile;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
           @NonNull HttpServletResponse response,
            @NonNull FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        boolean isSwagger = path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.equals("/swagger-ui.html");

        // En dev/local → no aplicar CSP en Swagger
        // En prod → aplicar CSP relajada en Swagger
        if (isSwagger) {
            if ("prod".equalsIgnoreCase(activeProfile)) {
                setIfAbsent(response, "Content-Security-Policy", SWAGGER_CSP);
            }
        } else {
            // Endpoints normales → CSP estricta
            setIfAbsent(response, "Content-Security-Policy", CSP);
        }

        setIfAbsent(response, "X-Content-Type-Options", "nosniff");
        setIfAbsent(response, "X-Frame-Options", "DENY");
        setIfAbsent(response, "Referrer-Policy", "no-referrer");
        setIfAbsent(response, "Permissions-Policy", PERMISSIONS_POLICY);
        setIfAbsent(response, "Cross-Origin-Resource-Policy", CORP);
        setIfAbsent(response, "Cross-Origin-Opener-Policy", COOP);
        setIfAbsent(response, "X-Permitted-Cross-Domain-Policies", XPCDP);

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
