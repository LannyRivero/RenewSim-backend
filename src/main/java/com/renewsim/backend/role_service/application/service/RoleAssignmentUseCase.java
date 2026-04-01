package com.renewsim.backend.role_service.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.renewsim.backend.role_service.application.command.AssignRoleCommand;
import com.renewsim.backend.role_service.application.command.RevokeRoleCommand;
import com.renewsim.backend.role_service.application.port.in.AssignRoleUseCase;
import com.renewsim.backend.role_service.application.port.in.RevokeRoleUseCase;
import com.renewsim.backend.role_service.application.port.out.UserServiceGateway;
import com.renewsim.backend.role_service.application.result.RoleAssignmentResultDTO;
import com.renewsim.backend.role_service.application.result.RoleRevocationResultDTO;
import com.renewsim.backend.shared.observability.RoleAuditService;
import com.renewsim.backend.user_service.web.dto.UpdateUserRolesRequestDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleAssignmentUseCase implements AssignRoleUseCase, RevokeRoleUseCase {

    private final UserServiceGateway userServiceGateway;
    private final RoleValidator roleValidator;
    private final RoleAuditService roleAuditService;

    @Override
    public RoleAssignmentResultDTO assignRoleToUser(AssignRoleCommand command) {
        roleValidator.validateRoleExists(command.roleId());

        UpdateUserRolesRequestDTO updateRequest = new UpdateUserRolesRequestDTO(
                List.of("ROLE_" + command.roleId()));
        userServiceGateway.updateUserRoles(command.targetUserId(), updateRequest);

        roleAuditService.roleAssigned(command.requesterId(), command.targetUserId(), "ROLE_" + command.roleId());

        return new RoleAssignmentResultDTO(
                command.targetUserId(),
                "ROLE_" + command.roleId(),
                true,
                "Role assigned successfully");
    }

    @Override
    public RoleRevocationResultDTO revokeRoleFromUser(RevokeRoleCommand command) {
        roleValidator.validateRoleExists(command.roleId());

        UpdateUserRolesRequestDTO updateRequest = new UpdateUserRolesRequestDTO(List.of());
        userServiceGateway.updateUserRoles(command.targetUserId(), updateRequest);

       roleAuditService.roleRevoked(command.requesterId(), command.targetUserId(), "ROLE_" + command.roleId());

        return new RoleRevocationResultDTO(
                command.targetUserId(),
                "ROLE_" + command.roleId(),
                true,
                "Role revoked successfully");
    }
}
