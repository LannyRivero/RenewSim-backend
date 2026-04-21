package com.renewsim.backend.auth_service.application.command;

/**
 * Command for the second step of 2FA login.
 * Validates OTP and issues JWT tokens.
 */
public record LoginStep2Command(String email, String otpCode) {}