package com.renewsim.backend.auth_service.application.dto;

/**
 * Internal DTO for user creation requests within auth_service.
 * Decouples auth_service from user_service DTOs.
 */
public record InternalUserCreateRequest(
        String username,
        String email,
        String rawPassword,
        String fullName) {
}
