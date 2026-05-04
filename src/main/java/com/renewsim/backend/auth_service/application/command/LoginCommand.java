package com.renewsim.backend.auth_service.application.command;

/**
 * Command to authenticate a user via email and password.
 *
 * @param email    user's email address
 * @param password user's plain-text password
 */
public record LoginCommand(
    String email,
    String password
) {
    public LoginCommand {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
    }
}