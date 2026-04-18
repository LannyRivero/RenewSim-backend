package com.renewsim.backend.auth_service.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record LoginStep2RequestDTO(
        @Email(message = "Must be a valid email address")
        @NotBlank(message = "Email is required")
        String email,

        @NotBlank(message = "OTP code is required")
        @Pattern(regexp = "\\d{6}", message = "OTP must be exactly 6 digits")
        String otpCode) {}