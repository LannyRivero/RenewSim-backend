package com.renewsim.backend.role_service.application.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRoleCommand(
        @NotBlank(message = "Role name cannot be blank")
        @Size(min = 3, max = 50, message = "Role name must be between 3 and 50 characters")
        String name,

        @Size(max = 255, message = "Role description cannot exceed 255 characters")
        String description) {
        public CreateRoleCommand(String name) {
                this(name, null);
        }
}
