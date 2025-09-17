package com.renewsim.backend.auth_service.infrastructure.handler;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import com.renewsim.backend.shared.observability.AuthAuditLogger;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomLogoutHandler implements LogoutSuccessHandler {


    @Override
    public void onLogoutSuccess(HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {
        if (authentication != null) {
            AuthAuditLogger.infoAuthLogout(authentication.getName(), request.getRemoteAddr());
        }
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
