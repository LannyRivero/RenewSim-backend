package com.renewsim.backend.role_service.domain.service;

import com.renewsim.backend.shared.domain.vo.RoleName;
import com.renewsim.backend.user_service.domain.model.User;
import com.renewsim.backend.shared.exception.ResourceNotFoundException;

/**
 * Domain service for managing role assignments.
 * Coordinates role assignment operations with proper validation.
 */
public class RoleAssignmentService {

    /**
     * Assigns a role to a user.
     * 
     * @param user the user to receive the role
     * @param role the role to assign
     * @throws ResourceNotFoundException if user or role is null
     * @throws IllegalStateException if user already has the role
     */
    public void assignRoleToUser(User user, RoleName role) {
        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }
        if (role == null) {
            throw new ResourceNotFoundException("Role not found");
        }

        if (user.hasRole(role)) {
            throw new IllegalStateException("User already has role: " + role.name());
        }

        user.addRole(role);
    }

    /**
     * Removes a role from a user.
     * 
     * @param user the user to remove the role from
     * @param role the role to remove
     * @throws ResourceNotFoundException if user or role is null
     * @throws IllegalStateException if user does not have the role
     */
    public void removeRoleFromUser(User user, RoleName role) {
        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }
        if (role == null) {
            throw new ResourceNotFoundException("Role not found");
        }

        if (!user.hasRole(role)) {
            throw new IllegalStateException("User does not have role: " + role.name());
        }

        user.removeRole(role);
    }
}