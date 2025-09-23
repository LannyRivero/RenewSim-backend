package com.renewsim.backend.role_service.application.service;

import com.renewsim.backend.role_service.application.port.in.*;
import com.renewsim.backend.role_service.application.port.out.RoleRepositoryPort;
import com.renewsim.backend.role_service.domain.model.Role;
import com.renewsim.backend.role_service.domain.model.RoleName;
import com.renewsim.backend.role_service.domain.policy.RolePolicy;
import com.renewsim.backend.role_service.domain.policy.RoleValidator;
import com.renewsim.backend.role_service.dto.RoleDTO;
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

    public RoleServiceImpl(RoleRepositoryPort roleRepositoryPort, RoleValidator roleValidator) {
        this.roleRepositoryPort = roleRepositoryPort;
        this.roleValidator = roleValidator;
    }

    @Override
    public RoleDTO create(RoleDTO request) {
        RoleName roleName = RolePolicy.normalizeRoleName(request.name());

        roleValidator.validateRoleDoesNotExist(roleName);

        Role role = new Role(null, roleName);
        Role saved = roleRepositoryPort.save(role);

        return new RoleDTO(saved.id(), saved.name().name());
    }

    @Override
    public List<RoleDTO> getAll() {
        return roleRepositoryPort.findAll()
                .stream()
                .map(role -> new RoleDTO(role.id(), role.name().name()))
                .toList();
    }

    @Override
    public void assignRoleToUser(Long roleId, Long userId) {
        throw new UnsupportedOperationException("Assign role to user not yet implemented");
    }

    @Override
    public void delete(Long roleId) {
        roleValidator.validateRoleExists(roleId);

        roleRepositoryPort.deleteById(roleId);
    }
}
