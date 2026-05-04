package com.renewsim.backend.user_service.web.dto;

import java.util.Set;
import com.renewsim.backend.shared.domain.vo.RoleName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserCredentialsDTO(
        @NotNull Long id,
        @NotBlank String username,
        @NotBlank String email,
        @NotBlank String passwordHash,
        Set<RoleName> roles,
        String status,          
        Boolean emailVerified,  
        boolean enabled) {}     