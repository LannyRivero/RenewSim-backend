package com.renewsim.backend.auth_service.application.result;

/**
 * Result for login step 1.
 * Generic message to avoid revealing whether the email exists.
 */
public record LoginStep1ResultDTO(String message, int expiresInSeconds) {}