package com.renewsim.backend.role_service.application.result;

import java.util.List;

public record ManageUserRolesResultDTO(
        Long targetUserId,
        List<String> assignedRoles,
        List<String> revokedRoles,
        boolean success,
        String message
) {}
