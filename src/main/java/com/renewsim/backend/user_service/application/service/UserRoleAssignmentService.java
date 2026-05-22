package com.renewsim.backend.user_service.application.service;

import java.util.Set;

import com.renewsim.backend.shared.domain.vo.RoleName;
import com.renewsim.backend.shared.exception.ResourceNotFoundException;
import com.renewsim.backend.shared.exception.UserNotFoundException;
import com.renewsim.backend.user_service.application.port.in.AssignUserRoleUseCase;
import com.renewsim.backend.user_service.application.port.in.RemoveUserRoleUseCase;
import com.renewsim.backend.user_service.application.port.out.RoleCatalogPort;
import com.renewsim.backend.user_service.application.port.out.UserRepositoryPort;
import com.renewsim.backend.user_service.domain.model.User;
import com.renewsim.backend.user_service.domain.service.UserPolicy;
import com.renewsim.backend.user_service.web.dto.RoleSnapshot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserRoleAssignmentService implements AssignUserRoleUseCase, RemoveUserRoleUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final RoleCatalogPort roleCatalogPort;

    @Override
    public void assignRole(Long userId, Long roleId) {
        User user = findUser(userId);
        RoleName roleName = findRoleName(roleId);

        user.addRole(roleName);
        userRepositoryPort.save(user);
    }

    @Override
    public void removeRole(Long userId, Long roleId) {
        User user = findUser(userId);
        RoleName roleName = findRoleName(roleId);

        if (!user.hasRole(roleName)) {
            throw new ResourceNotFoundException(
                    "Role assignment not found for user id=" + userId + " and role id=" + roleId);
        }

        UserPolicy.ensureAtLeastOneAdminRemaining(
                user.getRoles(),
                rolesWithout(user, roleName),
                userRepositoryPort.countByRole(RoleName.ADMIN));

        user.removeRole(roleName);
        userRepositoryPort.save(user);
    }

    private User findUser(Long userId) {
        return userRepositoryPort.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id=" + userId + " not found"));
    }

    private RoleName findRoleName(Long roleId) {
        RoleSnapshot role = roleCatalogPort.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role with id=" + roleId + " not found"));

        RoleName roleName = RoleName.valueOf(role.name().toUpperCase());
        UserPolicy.ensureRoleAssignableToUser(roleName);

        return roleName;
    }

    private Set<RoleName> rolesWithout(User user, RoleName roleName) {
        Set<RoleName> roles = new java.util.HashSet<>(user.getRoles());
        roles.remove(roleName);
        return roles;
    }
}
