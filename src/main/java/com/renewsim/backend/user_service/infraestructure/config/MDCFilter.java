package com.renewsim.backend.user_service.infraestructure.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class MDCFilter implements Filter {

    private static final String TRACE_ID = "traceId";
    private static final String USER_ID = "userId";
    private static final String IP_ADDRESS = "ip";
    private static final String PATH = "path";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        try {
            HttpServletRequest httpReq = (HttpServletRequest) request;

            // Generar un traceId único por request
            String traceId = UUID.randomUUID().toString();
            MDC.put(TRACE_ID, traceId);

            // IP del cliente
            String ip = httpReq.getRemoteAddr();
            MDC.put(IP_ADDRESS, ip);

            // Path de la request
            MDC.put(PATH, httpReq.getRequestURI());

            // (Opcional) Si tienes sesión o seguridad activada:
            HttpSession session = httpReq.getSession(false);
            if (session != null) {
                Object userAttr = session.getAttribute("userId");
                if (userAttr != null) {
                    MDC.put(USER_ID, userAttr.toString());
                }
            }

            chain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}

