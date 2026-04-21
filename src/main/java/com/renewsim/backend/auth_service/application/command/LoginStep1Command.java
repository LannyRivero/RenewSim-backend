package com.renewsim.backend.auth_service.application.command;

/**
 * Command for the first step of 2FA login.
 * Validates credentials and triggers OTP generation.
 */
public record LoginStep1Command(String email, String password) {}