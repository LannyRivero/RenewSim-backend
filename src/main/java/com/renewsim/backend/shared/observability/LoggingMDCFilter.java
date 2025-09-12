package com.renewsim.backend.shared.observability;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class LoggingMDCFilter implements Filter {

    private static final String TRACE_ID = "traceId";
    private static final String USER = "user";
    private static final String ROLES = "roles";
    private static final String IP = "ip";
    private static final String PATH = "path";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        try {
            HttpServletRequest httpRequest = (HttpServletRequest) request;

            // traceId único por request
            MDC.put(TRACE_ID, UUID.randomUUID().toString());

            // ip y path
            MDC.put(IP, request.getRemoteAddr());
            MDC.put(PATH, httpRequest.getRequestURI());

            // usuario autenticado (si existe)
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                MDC.put("username", auth.getName()); // 👈 aquí solo el nombre
                MDC.put("roles", auth.getAuthorities().toString());
            } else {
                MDC.put("username", "N/A");
                MDC.put("roles", "N/A");
            }

            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
