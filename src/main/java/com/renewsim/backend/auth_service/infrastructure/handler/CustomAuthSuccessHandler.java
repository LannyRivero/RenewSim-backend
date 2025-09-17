package com.renewsim.backend.auth_service.infrastructure.handler;

import com.renewsim.backend.shared.observability.AuthAuditLogger;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomAuthSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        String clientIp = request.getRemoteAddr();
        String username = authentication.getName();

        AuthAuditLogger.infoAuthSuccess(username, clientIp);

        response.setStatus(HttpServletResponse.SC_OK);
    }
}


