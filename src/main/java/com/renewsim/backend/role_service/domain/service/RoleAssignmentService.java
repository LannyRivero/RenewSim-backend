package com.renewsim.backend.role_service.domain.service;

import com.renewsim.backend.shared.domain.vo.RoleName;
import com.renewsim.backend.user_service.domain.model.User;
import com.renewsim.backend.shared.exception.ResourceNotFoundException;

/**
 * Service class responsible for managing role assignments to users.
 * Provides functionality to assign roles to users with proper validation.
 */
public class RoleAssignmentService {

    /**
     * Assigns a specific role to a user.
     * 
     * @param role the role to be assigned to the user
     * @param user the user who will receive the role assignment
     * @throws ResourceNotFoundException if the role is null (role not found)
     * @throws ResourceNotFoundException if the user is null (user not found)
     */
    public User assignRoleToUser(User user, RoleName role) {
        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }
        if (role == null) {
            throw new ResourceNotFoundException("Role not found");
        }

        if (user.roles().contains(role)) {
            throw new IllegalStateException("User already has role: " + role.name());
        }

        return user.withAdditionalRole(role);
    }

    /**
     * Removes a specific role from a user.
     * 
     * @param role the role to be removed from the user
     * @param user the user who will lose the role assignment
     * @throws ResourceNotFoundException if the role is null (role not found)
     * @throws ResourceNotFoundException if the user is null (user not found)
     */
    public User removeRoleFromUser(User user, RoleName role) {
        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }
        if (role == null) {
            throw new ResourceNotFoundException("Role not found");
        }

        if (!user.roles().contains(role)) {
            throw new IllegalStateException("User does not have role: " + role.name());
        }

        return user.withoutRole(role);
    }
}

