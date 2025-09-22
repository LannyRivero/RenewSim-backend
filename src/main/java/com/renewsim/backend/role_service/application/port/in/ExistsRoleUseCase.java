package com.renewsim.backend.role_service.application.port.in;

import com.renewsim.backend.role_service.domain.model.RoleName;

public interface ExistsRoleUseCase {
    boolean existsByName(RoleName roleName);
}

