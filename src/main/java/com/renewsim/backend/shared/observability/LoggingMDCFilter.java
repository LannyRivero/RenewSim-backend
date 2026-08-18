package com.renewsim.backend.shared.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class LoggingMDCFilter extends OncePerRequestFilter {

    private static final String TRACE_ID = "traceId";
    private static final String USERNAME = "username";
    private static final String ROLES = "roles";
    private static final String IP = "ip";
    private static final String PATH = "path";
    private static final String METHOD = "method";
    private static final String USER_AGENT = "userAgent";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // Keep the internal trace id aligned with the public correlation id until
            // real distributed tracing is introduced.
            MDC.put(TRACE_ID, resolveTraceId());

            // 2. IP y Path
            MDC.put(IP, request.getRemoteAddr());
            MDC.put(PATH, request.getRequestURI());
            MDC.put(METHOD, request.getMethod());
            MDC.put(USER_AGENT, Optional.ofNullable(request.getHeader("User-Agent")).orElse("N/A"));

            // 3. Usuario autenticado (si existe)
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && !(auth instanceof AnonymousAuthenticationToken) && auth.isAuthenticated()) {
                MDC.put(USERNAME, auth.getName());

                Set<String> roles = auth.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toSet());
                MDC.put(ROLES, roles.toString());
            } else {
                MDC.put(USERNAME, "anonymous");
                MDC.put(ROLES, "N/A");
            }

            // Continuar la cadena
            filterChain.doFilter(request, response);

        } finally {
            // Siempre limpiar al final del request
            MDC.clear();
        }
    }

    private String resolveTraceId() {
        return Optional.ofNullable(CorrelationIdFilter.currentCorrelationId())
                .filter(value -> !value.isBlank())
                .orElse(UUID.randomUUID().toString());
    }
}

