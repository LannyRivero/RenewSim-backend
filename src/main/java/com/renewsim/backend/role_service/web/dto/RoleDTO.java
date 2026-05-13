package com.renewsim.backend.role_service.web.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RoleDTO(
        @NotNull(message = "Id cannot be null")
        Long id,

        @NotBlank(message = "Role name cannot be blank")
        String name,

        @Size(max = 255, message = "Role description cannot exceed 255 characters")
        String description,

        @NotNull(message = "Role createdAt cannot be null")
        LocalDateTime createdAt
) {
        public RoleDTO(Long id, String name) {
                this(id, name, null, LocalDateTime.now());
        }
}
