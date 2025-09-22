package com.renewsim.backend.user_service.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record UpdateUserRolesRequestDTO(
        @NotEmpty(message = "Roles list cannot be empty")
        List<@NotBlank(message = "Role name cannot be blank") String> roles
) {}

