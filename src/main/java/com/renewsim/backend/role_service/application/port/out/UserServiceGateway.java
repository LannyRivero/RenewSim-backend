package com.renewsim.backend.role_service.application.port.out;

import com.renewsim.backend.role_service.application.dto.UserRolesUpdateRequest;

public interface UserServiceGateway {
    void updateUserRoles(Long userId, UserRolesUpdateRequest request);

    void assignRole(Long userId, Long roleId);

    void removeRole(Long userId, Long roleId);
}

