package com.renewsim.backend.auth_service.application.result;

import java.util.Set;

/**
 * Result for login step 2.
 * Contains the issued JWT access token and metadata.
 */
public record LoginStep2Result(
        String accessToken,
        String tokenType,
        long expiresIn,
        String username,
        Set<String> roles,
        String rawRefreshToken) {}