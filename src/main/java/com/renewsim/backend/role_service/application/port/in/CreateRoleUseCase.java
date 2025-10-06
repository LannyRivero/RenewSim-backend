package com.renewsim.backend.role_service.application.port.in;

import com.renewsim.backend.role_service.application.command.CreateRoleCommand;
import com.renewsim.backend.role_service.application.result.RoleCreationResultDTO;

public interface CreateRoleUseCase {
    RoleCreationResultDTO  createRole(CreateRoleCommand command);
}


