package com.renewsim.backend.user_service.dto;

import java.util.Set;

public record UserCredentialsDTO(
        String username,
        String email,
        String passwordHash, 
        Set<String> roles,
        boolean enabled
) {}

