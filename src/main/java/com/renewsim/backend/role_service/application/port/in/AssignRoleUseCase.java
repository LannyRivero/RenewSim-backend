package com.renewsim.backend.role_service.application.port.in;

public interface AssignRoleUseCase {
    void assignRoleToUser(Long roleId, Long userId);
}

