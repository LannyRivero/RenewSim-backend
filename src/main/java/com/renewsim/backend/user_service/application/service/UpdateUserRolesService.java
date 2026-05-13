package com.renewsim.backend.user_service.application.service;

import com.renewsim.backend.shared.domain.vo.RoleName;
import com.renewsim.backend.shared.exception.UserNotFoundException;
import com.renewsim.backend.user_service.application.port.in.UpdateUserRolesUseCase;
import com.renewsim.backend.user_service.application.port.out.RoleCatalogPort;
import com.renewsim.backend.user_service.application.port.out.UserRepositoryPort;
import com.renewsim.backend.user_service.domain.model.User;
import com.renewsim.backend.user_service.web.dto.UpdateUserRolesRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UpdateUserRolesService implements UpdateUserRolesUseCase {

    private final UserRepositoryPort userRepositoryPort;
    private final RoleCatalogPort roleCatalogPort;

    @Override
    public void updateUserRoles(Long userId, UpdateUserRolesRequestDTO request) {
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with id=" + userId + " not found"));

        Set<RoleName> newRoles = request.roles().stream()
                .map(name -> {
                    RoleName roleName = RoleName.valueOf(name.toUpperCase());
                    if (!roleCatalogPort.existsByName(roleName)) {
                        throw new IllegalArgumentException("Role not found: " + name);
                    }
                    return roleName;
                })
                .collect(Collectors.toSet());

        new HashSet<>(user.getRoles()).forEach(user::removeRole);
        newRoles.forEach(user::addRole);

        userRepositoryPort.save(user);
    }
}
