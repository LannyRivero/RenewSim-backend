package com.renewsim.backend.auth_service.application.port.out;

import java.util.Optional;
import com.renewsim.backend.auth_service.domain.AuthenticatedUser;

public interface TokenProvider {
    String generate(AuthenticatedUser user);

    Optional<AuthenticatedUser> validate(String token);

    long expiresInSeconds();

    /**
     * Extracts the JTI (JWT ID) claim from a token without full validation.
     * Returns empty if the token is malformed.
     */
    Optional<String> extractJti(String token);

    /**
     * Extracts the expiration time as epoch seconds.
     * Returns empty if the token is malformed.
     */
    Optional<Long> extractExpirationEpochSeconds(String token);
}