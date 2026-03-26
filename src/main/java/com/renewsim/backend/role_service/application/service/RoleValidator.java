package com.renewsim.backend.role_service.application.service;

import org.springframework.stereotype.Service;

import com.renewsim.backend.role_service.application.port.out.RoleRepositoryPort;
import com.renewsim.backend.role_service.domain.exception.LastAdminRemovalException;
import com.renewsim.backend.shared.domain.vo.RoleName;
import com.renewsim.backend.shared.exception.RoleAlreadyExistsException;
import com.renewsim.backend.shared.exception.RoleNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleValidator {

    private final RoleRepositoryPort roleRepositoryPort;

    /**
     * Validates that a role with this name does not already exist in the system.
     */
    public void validateRoleDoesNotExist(RoleName roleName) {
        roleRepositoryPort.findByName(roleName).ifPresent(existing -> {
            throw new RoleAlreadyExistsException("Role already exists: " + roleName.name());
        });
    }

    /** Validates that a role with the given id exists in the system. */
    public void validateRoleExists(Long roleId) {
        if (roleRepositoryPort.findById(roleId).isEmpty()) {
            throw new RoleNotFoundException("Role with id=" + roleId + " not found");
        }
    }

    /** Validates that we are not removing the last ADMIN role in the system. */
    public void validateNotRemovingLastAdmin(long totalAdmins, RoleName roleNameToRemove) {
        if (roleNameToRemove == RoleName.ADMIN && totalAdmins <= 1) {
            throw new LastAdminRemovalException("Cannot remove the last ADMIN role");
        }
    }
}
