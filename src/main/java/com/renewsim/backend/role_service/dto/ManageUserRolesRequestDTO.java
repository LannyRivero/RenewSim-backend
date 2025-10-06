package com.renewsim.backend.role_service.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ManageUserRolesRequestDTO(
        @NotNull Long requesterId,
        @NotNull Long targetUserId,
        List<Long> rolesToAssign,
        List<Long> rolesToRevoke
) {}
