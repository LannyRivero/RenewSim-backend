package com.renewsim.backend.user_service.dto;

import java.util.Set;

import jakarta.validation.constraints.NotBlank;

public record UserCredentialsDTO(

        @NotBlank (message = "Username is mandatory") String username,
        @NotBlank (message = "Email is mandatory") String email,
        @NotBlank (message = "Password is mandatory") String passwordHash,
        Set<String> roles,
        boolean enabled
) {}

