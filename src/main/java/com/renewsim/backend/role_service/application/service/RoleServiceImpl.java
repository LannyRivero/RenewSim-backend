package com.renewsim.backend.role_service.application.service;

import com.renewsim.backend.role_service.application.port.in.*;
import com.renewsim.backend.role_service.application.port.out.RoleRepositoryPort;
import com.renewsim.backend.role_service.domain.model.Role;
import com.renewsim.backend.role_service.domain.model.RoleName;
import com.renewsim.backend.role_service.dto.RoleDTO;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RoleServiceImpl implements 
        CreateRoleUseCase, 
        GetRolesUseCase, 
        AssignRoleUseCase, 
        DeleteRoleUseCase {

    private final RoleRepositoryPort roleRepositoryPort;

    public RoleServiceImpl(RoleRepositoryPort roleRepositoryPort) {
        this.roleRepositoryPort = roleRepositoryPort;
    }

    @Override
    public RoleDTO createRole(RoleDTO request) {
        Role role = new Role(RoleName.valueOf(request.getName()));
        Role saved = roleRepositoryPort.save(role);
        return new RoleDTO(saved.getId(), saved.getName().name());
    }

    @Override
    public List<RoleDTO> getAllRoles() {
        return roleRepositoryPort.findAll()
                .stream()
                .map(role -> new RoleDTO(role.getId(), role.getName().name()))
                .collect(Collectors.toList());
    }

    @Override
    public void assignRoleToUser(Long roleId, Long userId) {
        // TODO: Integrar con UserService
        throw new UnsupportedOperationException("Assign role to user not yet implemented");
    }

    @Override
    public void deleteRole(Long roleId) {
        roleRepositoryPort.deleteById(roleId);
    }
}
