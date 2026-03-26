package com.renewsim.backend.user_service.dto;

import java.util.Set;

import com.renewsim.backend.shared.domain.vo.RoleName;

import jakarta.validation.constraints.NotBlank;

public record UserCredentialsDTO(

        @NotBlank (message = "Username is mandatory") String username,
        @NotBlank (message = "Email is mandatory") String email,
        @NotBlank (message = "Password is mandatory") String passwordHash,
        Set<RoleName> roles,
        boolean enabled
) {}

