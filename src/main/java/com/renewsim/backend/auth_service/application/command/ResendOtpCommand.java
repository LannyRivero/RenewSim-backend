package com.renewsim.backend.auth_service.application.command;

/**
 * Command for resending an OTP code to the user.
 */
public record ResendOtpCommand(String email) {}