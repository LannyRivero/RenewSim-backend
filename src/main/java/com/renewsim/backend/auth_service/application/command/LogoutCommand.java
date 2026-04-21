package com.renewsim.backend.auth_service.application.command;

/**
 * Command for logging out a user.
 * Contains the raw Bearer token from the Authorization header.
 */
public record LogoutCommand(String accessToken, String username) {}