package com.renewsim.backend.role_service.application.port.in;

import com.renewsim.backend.role_service.application.command.AssignRoleCommand;
import com.renewsim.backend.role_service.application.result.RoleAssignmentResultDTO;

public interface AssignRoleUseCase {
    RoleAssignmentResultDTO  assignRoleToUser(AssignRoleCommand command);
}

