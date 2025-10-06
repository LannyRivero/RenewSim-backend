package com.renewsim.backend.role_service.application.result;

public record RoleDeletionResultDTO(
    Long roleId,
    boolean success,
    String message
) {}

