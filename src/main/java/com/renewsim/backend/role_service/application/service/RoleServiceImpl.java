package com.renewsim.backend.role_service.application.service;

import com.renewsim.backend.role_service.application.port.in.*;
import com.renewsim.backend.role_service.application.port.out.RoleRepositoryPort;
import com.renewsim.backend.role_service.domain.model.Role;
import com.renewsim.backend.role_service.domain.model.RoleName;
import com.renewsim.backend.role_service.dto.RoleDTO;
import com.renewsim.backend.shared.exception.RoleAlreadyExistsException;
import com.renewsim.backend.shared.exception.RoleNotFoundException;
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

    public RoleServiceImpl(RoleRepositoryPort roleRepositoryPort) {
        this.roleRepositoryPort = roleRepositoryPort;
    }

    @Override
    public RoleDTO create(RoleDTO request) {
        RoleName roleName = RoleName.valueOf(request.name().toUpperCase());

        // 🚨 Validar duplicados
        roleRepositoryPort.findByName(roleName).ifPresent(existing -> {
            throw new RoleAlreadyExistsException("Role already exists: " + roleName.name());
        });

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
        // 🚨 Validar existencia antes de borrar
        if (roleRepositoryPort.findById(roleId).isEmpty()) {
            throw new RoleNotFoundException("Role with id=" + roleId + " not found");
        }
        roleRepositoryPort.deleteById(roleId);
    }
}
