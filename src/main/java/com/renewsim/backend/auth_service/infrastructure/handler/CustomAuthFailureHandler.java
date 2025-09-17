package com.renewsim.backend.auth_service.infrastructure.handler;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.renewsim.backend.shared.observability.AuthAuditLogger;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAuthFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String username = request.getParameter("username");
        String clientIp = request.getRemoteAddr();

        AuthAuditLogger.warnAuthFailure(exception.getMessage(), clientIp, username);

        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication failed");
    }
}
