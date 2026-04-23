package com.renewsim.backend.auth_service.application.result;

import java.util.Set;

/**
 * Result for refresh token rotation.
 * Contains the new JWT access token.
 */
public record RefreshTokenResult(
        String accessToken,
        String tokenType,
        long expiresIn,
        String username,
        Set<String> roles,
        String rawRefreshToken) {}