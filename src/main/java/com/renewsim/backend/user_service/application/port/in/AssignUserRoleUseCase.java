package com.renewsim.backend.user_service.application.port.in;

public interface AssignUserRoleUseCase {

    void assignRole(Long userId, Long roleId);
}
