package com.renewsim.backend.role_service.dto;

import jakarta.validation.constraints.NotBlank;

public record RoleCreateRequestDTO(
        @NotBlank(message = "Role name cannot be blank")
        String name
) {}

