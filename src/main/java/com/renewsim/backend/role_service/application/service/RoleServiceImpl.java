package com.renewsim.backend.role_service.application.service;

import com.renewsim.backend.role_service.application.port.in.*;
import com.renewsim.backend.role_service.application.port.out.RoleRepositoryPort;
import com.renewsim.backend.role_service.application.port.out.UserServiceGateway;
import com.renewsim.backend.role_service.domain.model.Role;
import com.renewsim.backend.role_service.domain.model.RoleName;
import com.renewsim.backend.role_service.domain.policy.RolePolicy;
import com.renewsim.backend.role_service.domain.policy.RoleValidator;
import com.renewsim.backend.role_service.dto.RoleDTO;
import com.renewsim.backend.role_service.infrastructure.mapper.RoleServiceMapper;
import com.renewsim.backend.shared.exception.RoleNotFoundException;
import com.renewsim.backend.user_service.dto.UpdateUserRolesRequestDTO;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RoleServiceImpl implements
        CreateRoleUseCase,
        GetRolesUseCase,
        AssignRoleUseCase,
        DeleteRoleUseCase {

    private final RoleRepositoryPort roleRepositoryPort;
    private final RoleValidator roleValidator;
    private final RoleServiceMapper roleMapper;
    private final UserServiceGateway userServiceGateway;

    public RoleServiceImpl(RoleRepositoryPort roleRepositoryPort, RoleValidator roleValidator,
            RoleServiceMapper roleMapper, UserServiceGateway userServiceGateway) {
        this.roleRepositoryPort = roleRepositoryPort;
        this.roleValidator = roleValidator;
        this.roleMapper = roleMapper;
        this.userServiceGateway = userServiceGateway;
    }

    @Override
    public RoleDTO create(RoleDTO request) {
        RoleName roleName = RolePolicy.normalizeRoleName(request.name());
        roleValidator.validateRoleDoesNotExist(roleName);

        Role role = new Role(null, roleName);
        Role saved = roleRepositoryPort.save(role);

        return roleMapper.toDTO(saved);
    }

    @Override
    public List<RoleDTO> getAll() {
        return roleRepositoryPort.findAll()
                .stream()
                .map(roleMapper::toDTO)
                .toList();
    }

    @Override
    public void assignRoleToUser(Long roleId, Long userId) {
        Role role = roleRepositoryPort.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException("Role not found with id: " + roleId));

        UpdateUserRolesRequestDTO request = new UpdateUserRolesRequestDTO(
                List.of(role.name().name()));

        userServiceGateway.updateUserRoles(userId, request);
    }

    @Override
    public void delete(Long roleId) {
        roleValidator.validateRoleExists(roleId);
        roleRepositoryPort.deleteById(roleId);
    }
}
