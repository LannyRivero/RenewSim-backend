package com.renewsim.backend.role_service.domain.service;

import com.renewsim.backend.role_service.domain.model.Role;
import com.renewsim.backend.shared.domain.vo.RoleName;
import com.renewsim.backend.role_service.domain.policy.RolePolicy;
import com.renewsim.backend.role_service.application.port.out.RoleRepositoryPort;
import com.renewsim.backend.shared.exception.RoleAlreadyExistsException;
import com.renewsim.backend.shared.exception.RoleNotFoundException;

public class RoleDomainService {

    private final RoleRepositoryPort roleRepositoryPort;

    public RoleDomainService(RoleRepositoryPort roleRepositoryPort) {
        this.roleRepositoryPort = roleRepositoryPort;
    }

    public void ensureRoleDoesNotExist(RoleName roleName) {
        if (roleRepositoryPort.findByName(roleName).isPresent()) {
            throw new RoleAlreadyExistsException("Role already exists: " + roleName);
        }
    }

    public Role ensureRoleExists(Long roleId) {
        return roleRepositoryPort.findById(roleId)
                .orElseThrow(() -> new RoleNotFoundException("Role with id=" + roleId + " not found"));
    }

    public void ensureNotRemovingLastAdmin(RoleName roleName) {
        if (roleName == RoleName.ADMIN) {
            long totalAdmins = roleRepositoryPort.countByName(RoleName.ADMIN);
            if (totalAdmins <= 1) {
                throw new IllegalStateException("Cannot remove the last ADMIN role");
            }
        }
    }

    public Role createRole(String rawName) {
        return createRole(rawName, null);
    }

    public Role createRole(String rawName, String description) {
        RoleName normalized = RolePolicy.normalizeRoleName(rawName);
        return new Role(normalized, description);
    }
}

