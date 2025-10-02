package com.renewsim.backend.role_service.application.command;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ManageUserRolesCommand(
                @NotNull(message = "Requester ID cannot be null") @Positive(message = "Requester ID must be positive") Long requesterId,

                @NotNull(message = "Target User ID cannot be null") @Positive(message = "Target User ID must be positive") Long targetUserId,

                @Size(min = 1, message = "At least one role to assign is required") List<@NotNull @Positive Long> rolesToAssign,

                List<@NotNull @Positive Long> rolesToRevoke) {
}
