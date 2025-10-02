package com.renewsim.backend.role_service.application.command;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RevokeRoleCommand(
        @NotNull(message = "Requester ID cannot be null") @Positive(message = "Requester ID must be positive") Long requesterId,

        @NotNull(message = "Target User ID cannot be null") @Positive(message = "Target User ID must be positive") Long targetUserId,

        @NotNull(message = "Role ID cannot be null") @Positive(message = "Role ID must be positive") Long roleId) {
}
