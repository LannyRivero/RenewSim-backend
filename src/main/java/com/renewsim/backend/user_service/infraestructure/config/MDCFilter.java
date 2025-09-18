package com.renewsim.backend.user_service.infraestructure.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class MDCFilter implements Filter {

    private static final String TRACE_ID = "traceId";
    private static final String USERNAME = "username";
    private static final String ROLES = "roles";
    private static final String IP = "ip";
    private static final String PATH = "path";
    private static final String METHOD = "method";
    private static final String USER_AGENT = "userAgent";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        try {
            HttpServletRequest httpReq = (HttpServletRequest) request;

            // --- TraceId (propagado si existe, generado si no) ---
            String traceId = Optional.ofNullable(httpReq.getHeader("X-Trace-Id"))
                    .filter(id -> !id.isBlank())
                    .orElse(UUID.randomUUID().toString());
            MDC.put(TRACE_ID, traceId);

            // --- Client IP (X-Forwarded-For > RemoteAddr) ---
            String clientIp = Optional.ofNullable(httpReq.getHeader("X-Forwarded-For"))
                    .map(ip -> ip.split(",")[0].trim())
                    .orElse(httpReq.getRemoteAddr());
            MDC.put(IP, clientIp);

            // --- Request Path ---
            MDC.put(PATH, httpReq.getRequestURI());

            // --- HTTP Method ---
            MDC.put(METHOD, httpReq.getMethod());

            // --- User-Agent ---
            MDC.put(USER_AGENT, Optional.ofNullable(httpReq.getHeader("User-Agent")).orElse("N/A"));

            // --- User info (if authenticated) ---
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                    && !"anonymousUser".equals(authentication.getPrincipal())) {

                MDC.put(USERNAME, authentication.getName());

                String roles = authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(","));
                MDC.put(ROLES, roles);
            }

            chain.doFilter(request, response);

        } finally {
            MDC.clear();
        }
    }
}





