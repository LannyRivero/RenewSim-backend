package com.renewsim.backend.role_service.application.service;

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
        userServiceGateway.assignRole(command.targetUserId(), command.roleId());

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
        userServiceGateway.removeRole(command.targetUserId(), command.roleId());

       roleAuditService.roleRevoked(command.requesterId(), command.targetUserId(), "ROLE_" + command.roleId());

        return new RoleRevocationResultDTO(
                command.targetUserId(),
                "ROLE_" + command.roleId(),
                true,
                "Role revoked successfully");
    }
}
