package com.renewsim.backend.auth_service.web.dto;

import com.renewsim.backend.user_service.domain.model.UserStatus;

public record RegisterResponseDTO(
        Long id,
        String email,
        String fullName,
        UserStatus status,
        String message) {
}