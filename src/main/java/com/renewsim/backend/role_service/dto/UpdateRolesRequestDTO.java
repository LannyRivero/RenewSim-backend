package com.renewsim.backend.role_service.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record UpdateRolesRequestDTO(
        @NotEmpty(message = "The roles list cannot be empty")
        List<String> roles
) {}


