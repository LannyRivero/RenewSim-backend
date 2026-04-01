package com.renewsim.backend.role_service.web.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record UpdateRolesRequestDTO(
        @NotEmpty(message = "The roles list cannot be empty")
        List<
            @NotBlank(message = "Role name cannot be blank") String
        > roles
) {}


