package com.renewsim.backend.role_service.domain.policy;

import com.renewsim.backend.role_service.application.port.out.RoleRepositoryPort;
import com.renewsim.backend.role_service.domain.model.RoleName;
import com.renewsim.backend.shared.exception.RoleAlreadyExistsException;

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
}
