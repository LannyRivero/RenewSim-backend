package com.renewsim.backend.auth_service.domain;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.renewsim.backend.auth_service.web.dto.CredentialsRequest;
import com.renewsim.backend.shared.error.ErrorMessageFactory;
import com.renewsim.backend.shared.exception.AuthenticationException;

import lombok.RequiredArgsConstructor;

import static com.renewsim.backend.auth_service.domain.error.AuthErrorCode.AUTH_INVALID_CREDENTIALS;

@Component
@RequiredArgsConstructor
public class AuthValidator {

    private final PasswordEncoder passwordEncoder;

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

    public void validatePassword(String rawPassword, String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new AuthenticationException(
                ErrorMessageFactory.build(AUTH_INVALID_CREDENTIALS));
        }
    }

    public String encodePassword(String rawPassword) {
    return passwordEncoder.encode(rawPassword);
}

}
