package com.renewsim.backend.auth_service.web.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for email verification.
 */
public record VerifyEmailRequest(
    @NotBlank(message = "Token is required")
    String token
) {}