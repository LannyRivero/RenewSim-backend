package com.renewsim.backend.role_service.application.result;

public record RoleRevocationResultDTO(
    Long targetUserId,
    String roleRevoked,
    boolean success,
    String message
) {}
