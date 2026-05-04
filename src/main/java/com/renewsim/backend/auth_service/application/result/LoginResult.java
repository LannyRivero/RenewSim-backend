package com.renewsim.backend.auth_service.application.result;

import java.util.Set;

/**
 * Result of a successful login operation.
 *
 * @param accessToken  JWT access token
 * @param refreshToken JWT refresh token (raw, to be stored in HttpOnly cookie)
 * @param tokenType    always "Bearer"
 * @param expiresIn    access token TTL in seconds
 * @param userId       authenticated user ID
 * @param username     authenticated username
 * @param roles        user's role names
 */
public record LoginResult(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        Long userId,
        String username,
        Set<String> roles) {
}