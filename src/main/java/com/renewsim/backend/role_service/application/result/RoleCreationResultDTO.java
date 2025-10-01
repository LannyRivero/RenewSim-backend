package com.renewsim.backend.role_service.application.result;

public record RoleCreationResultDTO(
    Long roleId,
    String roleName,
    boolean success,
    String message
) {}
