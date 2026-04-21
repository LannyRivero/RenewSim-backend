package com.renewsim.backend.auth_service.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResendOtpRequestDTO(
        @Email(message = "Must be a valid email address")
        @NotBlank(message = "Email is required")
        String email) {}