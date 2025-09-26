package com.renewsim.backend.role_service.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.renewsim.backend.role_service.application.port.in.CreateRoleUseCase;
import com.renewsim.backend.role_service.application.port.in.DeleteRoleUseCase;
import com.renewsim.backend.role_service.application.port.in.GetRolesUseCase;
import com.renewsim.backend.role_service.application.port.out.RoleRepositoryPort;
import com.renewsim.backend.role_service.domain.model.Role;
import com.renewsim.backend.role_service.domain.model.RoleName;
import com.renewsim.backend.role_service.domain.policy.RolePolicy;
import com.renewsim.backend.role_service.domain.policy.RoleValidator;
import com.renewsim.backend.role_service.dto.RoleDTO;
import com.renewsim.backend.role_service.infrastructure.mapper.RoleServiceMapper;

@Service
@Transactional
public class RoleServiceImpl implements
        CreateRoleUseCase,
        GetRolesUseCase,
        DeleteRoleUseCase {

    private final RoleRepositoryPort roleRepositoryPort;
    private final RoleValidator roleValidator;
    private final RoleServiceMapper roleMapper;

    public RoleServiceImpl(RoleRepositoryPort roleRepositoryPort, RoleValidator roleValidator,
            RoleServiceMapper roleMapper) {
        this.roleRepositoryPort = roleRepositoryPort;
        this.roleValidator = roleValidator;
        this.roleMapper = roleMapper;
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
    public void delete(Long roleId) {
        roleValidator.validateRoleExists(roleId);
        roleRepositoryPort.deleteById(roleId);
    }
}
