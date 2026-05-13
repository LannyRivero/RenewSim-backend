package com.renewsim.backend.role_service.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RoleCreateRequestDTO(
        @NotBlank(message = "Role name cannot be blank")
        @Size(max = 32, message = "Role name cannot exceed 32 characters")
        String name,

        @Size(max = 255, message = "Role description cannot exceed 255 characters")
        String description
) {
        public RoleCreateRequestDTO(String name) {
                this(name, null);
        }
}

