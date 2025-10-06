package com.renewsim.backend.role_service.application.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRoleCommand(
        @NotBlank(message = "Role name cannot be blank") @Size(min = 3, max = 50, message = "Role name must be between 3 and 50 characters") String name) {
}
