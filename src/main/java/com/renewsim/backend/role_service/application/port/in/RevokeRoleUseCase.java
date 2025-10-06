package com.renewsim.backend.role_service.application.port.in;

import com.renewsim.backend.role_service.application.command.RevokeRoleCommand;
import com.renewsim.backend.role_service.application.result.RoleRevocationResultDTO;

public interface RevokeRoleUseCase {
    RoleRevocationResultDTO revokeRoleFromUser(RevokeRoleCommand command);
}
