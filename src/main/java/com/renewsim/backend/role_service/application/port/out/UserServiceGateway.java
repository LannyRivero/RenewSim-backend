package com.renewsim.backend.role_service.application.port.out;

import com.renewsim.backend.user_service.web.dto.UpdateUserRolesRequestDTO;

public interface UserServiceGateway {
    void updateUserRoles(Long userId, UpdateUserRolesRequestDTO request);

    void assignRole(Long userId, Long roleId);

    void removeRole(Long userId, Long roleId);
}

