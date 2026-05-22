package com.renewsim.backend.user_service.application.port.in;

public interface RemoveUserRoleUseCase {

    void removeRole(Long userId, Long roleId);
}
