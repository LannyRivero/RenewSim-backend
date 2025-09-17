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
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class MDCFilter implements Filter {

    private static final String TRACE_ID = "traceId";
    private static final String USERNAME = "username";
    private static final String ROLES = "roles";
    private static final String IP = "ip";
    private static final String PATH = "path";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        try {
            HttpServletRequest httpReq = (HttpServletRequest) request;

            // traceId único por request
            MDC.put(TRACE_ID, UUID.randomUUID().toString());

            // IP y path
            MDC.put(IP, httpReq.getRemoteAddr());
            MDC.put(PATH, httpReq.getRequestURI());

            // Usuario autenticado (si lo hay)
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


