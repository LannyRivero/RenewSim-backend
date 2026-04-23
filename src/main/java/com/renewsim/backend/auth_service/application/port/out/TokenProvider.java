package com.renewsim.backend.auth_service.application.port.out;

import java.util.Optional;
import java.util.Set;
import com.renewsim.backend.auth_service.domain.AuthenticatedUser;

public interface TokenProvider {
    String generate(AuthenticatedUser user);

    Optional<AuthenticatedUser> validate(String token);

    long expiresInSeconds();

    Optional<String> extractJti(String token);

    Optional<Long> extractExpirationEpochSeconds(String token);

    String generateServiceToken(String serviceName, Set<String> scopes);
}