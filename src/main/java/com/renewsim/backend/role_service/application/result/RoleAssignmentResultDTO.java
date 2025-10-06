package com.renewsim.backend.role_service.application.result;

public record RoleAssignmentResultDTO(
    Long targetUserId,
    String roleAssigned,
    boolean success,
    String message
) {}

