package com.renewsim.backend.role_service.application.service;

import com.renewsim.backend.role_service.application.command.ManageUserRolesCommand;
import com.renewsim.backend.role_service.application.port.in.ManageUserRolesUseCase;
import com.renewsim.backend.role_service.application.port.out.UserServiceGateway;
import com.renewsim.backend.role_service.application.result.ManageUserRolesResultDTO;
import com.renewsim.backend.shared.observability.RoleAuditLogger;
import com.renewsim.backend.user_service.web.dto.UpdateUserRolesRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ManageUserRolesCommandService implements ManageUserRolesUseCase {

    private final UserServiceGateway userServiceGateway;

    @Override
    public ManageUserRolesResultDTO manageRoles(ManageUserRolesCommand command) {
        List<String> finalRoles = command.rolesToAssign().stream()
                .map(r -> "ROLE_" + r)
                .toList();

        UpdateUserRolesRequestDTO request = new UpdateUserRolesRequestDTO(finalRoles);
        userServiceGateway.updateUserRoles(command.targetUserId(), request);

        RoleAuditLogger.rolesBatchUpdated(
                command.requesterId(),
                command.targetUserId(),
                finalRoles,
                command.rolesToRevoke().stream().map(r -> "ROLE_" + r).toList()
        );

        return new ManageUserRolesResultDTO(
                command.targetUserId(),
                finalRoles,
                command.rolesToRevoke().stream().map(r -> "ROLE_" + r).toList(),
                true,
                "User roles updated in batch"
        );
    }
}
