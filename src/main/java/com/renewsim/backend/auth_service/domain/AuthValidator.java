package com.renewsim.backend.auth_service.domain;

import org.springframework.stereotype.Component;

import com.renewsim.backend.auth_service.web.dto.AuthRequestDTO;
import com.renewsim.backend.shared.exception.AuthenticationException;

@Component
public class AuthValidator {
    public void validateCredentials(AuthRequestDTO request) {
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new AuthenticationException("Username cannot be blank");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new AuthenticationException("Password cannot be blank");

        }
    }

    public void validateUserEnable(boolean enabled) {
        if (!enabled) {
            throw new AuthenticationException("User is disabled");
        }

    }

}
