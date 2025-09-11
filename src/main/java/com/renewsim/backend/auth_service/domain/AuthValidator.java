package com.renewsim.backend.auth_service.domain;

import org.springframework.stereotype.Component;
import com.renewsim.backend.auth_service.web.dto.CredentialsRequest;

@Component
public class AuthValidator {

    public void validateCredentials(CredentialsRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            throw new IllegalArgumentException("Username is required");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
    }

    public void validateUserEnable(boolean enabled) {
        if (!enabled) {
            throw new IllegalStateException("User is disabled");
        }
    }
}
