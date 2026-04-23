package com.renewsim.backend.auth_service.application.result;

/**
 * Result for OTP resend.
 * Generic message to avoid revealing user existence.
 */
public record ResendOtpResult(String message, int expiresInSeconds) {}