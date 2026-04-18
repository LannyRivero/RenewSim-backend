package com.renewsim.backend.user_service.web.dto;

import java.util.Set;
import com.renewsim.backend.shared.domain.vo.RoleName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserCredentialsDTO(
        @NotNull Long id,
        @NotBlank(message = "Username is mandatory") String username,
        @NotBlank(message = "Email is mandatory") String email,
        @NotBlank(message = "Password is mandatory") String passwordHash,
        Set<RoleName> roles,
        boolean enabled) {}