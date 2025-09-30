package com.renewsim.backend.role_service.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.renewsim.backend.role_service.application.port.in.AssignRoleUseCase;
import com.renewsim.backend.role_service.application.port.out.RoleRepositoryPort;
import com.renewsim.backend.role_service.application.port.out.UserServiceGateway;
import com.renewsim.backend.role_service.domain.model.Role;
import com.renewsim.backend.shared.exception.RoleNotFoundException;
import com.renewsim.backend.user_service.dto.UpdateUserRolesRequestDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleAssignmentService implements AssignRoleUseCase {

    private final RoleRepositoryPort roleRepositoryPort;
    private final UserServiceGateway userServiceGateway;

    @Override
    public void assignRoleToUser(Long roleId, Long userId) {
        Role role = roleRepositoryPort.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException("Role not found with id: " + roleId));

        UpdateUserRolesRequestDTO request = new UpdateUserRolesRequestDTO(
                List.of(role.name().name()));

        userServiceGateway.updateUserRoles(userId, request);
    }
}
