package com.renewsim.backend.role_service.application.port.in;

import com.renewsim.backend.role_service.application.command.ManageUserRolesCommand;
import com.renewsim.backend.role_service.application.result.ManageUserRolesResultDTO;

public interface ManageUserRolesUseCase {
    ManageUserRolesResultDTO manageRoles(ManageUserRolesCommand command);
}
