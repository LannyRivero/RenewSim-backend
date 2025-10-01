package com.renewsim.backend.role_service.application.port.in;

import com.renewsim.backend.role_service.application.command.CreateRoleCommand;
import com.renewsim.backend.role_service.dto.RoleDTO;

public interface CreateRoleUseCase {
    RoleDTO createRole(CreateRoleCommand command);
}


