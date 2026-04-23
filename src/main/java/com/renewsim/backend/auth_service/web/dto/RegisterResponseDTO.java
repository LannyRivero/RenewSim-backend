package com.renewsim.backend.auth_service.web.dto;

import com.renewsim.backend.auth_service.domain.model.AuthUserStatus;

public record RegisterResponseDTO(
                Long id,
                String email,
                String fullName,
                AuthUserStatus status,
                String message) {
}