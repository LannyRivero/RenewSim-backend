package com.renewsim.backend.auth_service.web.dto;

import jakarta.validation.constraints.NotBlank;

public record ActivateAccountRequestDTO(
        @NotBlank(message = "Activation token is required")
        String token) {}