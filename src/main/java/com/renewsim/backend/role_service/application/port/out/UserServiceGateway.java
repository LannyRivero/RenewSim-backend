package com.renewsim.backend.role_service.application.port.out;

import com.renewsim.backend.user_service.dto.UpdateUserRolesRequestDTO;

public interface UserServiceGateway {
    void updateUserRoles(Long userId, UpdateUserRolesRequestDTO request);
}

