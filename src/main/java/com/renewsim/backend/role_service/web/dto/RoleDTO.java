package com.renewsim.backend.role_service.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RoleDTO(
        @NotNull(message = "Id cannot be null")
        Long id,

        @NotBlank(message = "Role name cannot be blank")
        String name
) {}
