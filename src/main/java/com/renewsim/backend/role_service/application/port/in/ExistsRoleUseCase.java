package com.renewsim.backend.role_service.application.port.in;

import com.renewsim.backend.shared.domain.vo.RoleName;

public interface ExistsRoleUseCase {
    boolean existsByName(RoleName roleName);
}

