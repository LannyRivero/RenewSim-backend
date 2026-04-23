package com.renewsim.backend.auth_service.application.result;

import java.time.Instant;
import java.util.Set;

public record AuthResult(
        String token,
        String tokenType,
        Instant expiresAt,
        String username,
        Set<String> roles,
        Set<String> scopes) {}
