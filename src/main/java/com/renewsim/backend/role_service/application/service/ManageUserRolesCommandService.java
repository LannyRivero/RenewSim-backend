package com.renewsim.backend.role_service.application.service;

import com.renewsim.backend.role_service.application.command.ManageUserRolesCommand;
import com.renewsim.backend.role_service.application.port.in.ManageUserRolesUseCase;
import com.renewsim.backend.role_service.application.port.out.RoleRepositoryPort;
import com.renewsim.backend.role_service.application.port.out.UserServiceGateway;
import com.renewsim.backend.role_service.application.result.ManageUserRolesResultDTO;
import com.renewsim.backend.shared.exception.RoleNotFoundException;
import com.renewsim.backend.shared.observability.RoleAuditLogger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ManageUserRolesCommandService implements ManageUserRolesUseCase {

    private final UserServiceGateway userServiceGateway;
    private final RoleRepositoryPort roleRepositoryPort;

    @Override
    public ManageUserRolesResultDTO manageRoles(ManageUserRolesCommand command) {
        List<Long> rolesToAssign = command.rolesToAssign() == null ? List.of() : command.rolesToAssign();
        List<Long> rolesToRevoke = command.rolesToRevoke() == null ? List.of() : command.rolesToRevoke();

        List<String> assignedRoles = rolesToAssign.stream()
                .map(this::resolveRoleName)
                .toList();

        List<String> revokedRoles = rolesToRevoke.stream()
                .map(this::resolveRoleName)
                .toList();

        rolesToAssign.forEach(roleId -> userServiceGateway.assignRole(command.targetUserId(), roleId));
        rolesToRevoke.forEach(roleId -> userServiceGateway.removeRole(command.targetUserId(), roleId));

        RoleAuditLogger.rolesBatchUpdated(
                command.requesterId(),
                command.targetUserId(),
                assignedRoles,
                revokedRoles
        );

        return new ManageUserRolesResultDTO(
                command.targetUserId(),
                assignedRoles,
                revokedRoles,
                true,
                "User roles updated in batch"
        );
    }

    private String resolveRoleName(Long roleId) {
        return roleRepositoryPort.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException("Role with id=" + roleId + " not found"))
                .name()
                .name();
    }
}
