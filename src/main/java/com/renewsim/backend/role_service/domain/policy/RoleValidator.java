package com.renewsim.backend.role_service.domain.policy;

import org.springframework.stereotype.Component;

import com.renewsim.backend.role_service.application.port.out.RoleRepositoryPort;
import com.renewsim.backend.role_service.domain.model.RoleName;
import com.renewsim.backend.shared.exception.RoleAlreadyExistsException;
import com.renewsim.backend.shared.exception.RoleNotFoundException;

@Component
public class RoleValidator {
    private final RoleRepositoryPort roleRepositoryPort;

    public RoleValidator(RoleRepositoryPort roleRepositoryPort) {
        this.roleRepositoryPort = roleRepositoryPort;
    }

    public void validateRoleDoesNotExist(RoleName roleName) {
        roleRepositoryPort.findByName(roleName).ifPresent(existing -> {
            throw new RoleAlreadyExistsException("Role already exists: " + roleName.name());
        });

    }

    public void validateRoleExists(Long roleId) {
        if (roleRepositoryPort.findById(roleId).isEmpty()) {
            throw new RoleNotFoundException("Role with id=" + roleId + " not found");
        }
    }
}
