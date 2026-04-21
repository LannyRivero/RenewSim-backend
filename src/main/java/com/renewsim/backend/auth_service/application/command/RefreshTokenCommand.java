package com.renewsim.backend.auth_service.application.command;

/**
 * Command for rotating a refresh token.
 * Contains the raw refresh token value from the HttpOnly cookie.
 */
public record RefreshTokenCommand(String refreshToken) {}