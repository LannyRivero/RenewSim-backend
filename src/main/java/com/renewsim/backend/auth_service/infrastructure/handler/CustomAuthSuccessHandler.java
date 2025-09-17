package com.renewsim.backend.auth_service.infrastructure.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.renewsim.backend.shared.observability.AuthAuditLogger;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Component
public class CustomAuthSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger("AUTH_AUDIT");

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String clientIp = request.getRemoteAddr();
        String username = authentication.getName();

        log.info("auth_success username={} clientIp={} correlationId={}",
                username,
                clientIp,
                AuthAuditLogger.currentCorrelationId());

        response.setStatus(HttpServletResponse.SC_OK);
    }
}

