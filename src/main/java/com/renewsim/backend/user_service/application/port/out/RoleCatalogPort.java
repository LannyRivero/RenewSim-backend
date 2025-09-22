package com.renewsim.backend.user_service.application.port.out;

import com.renewsim.backend.role_service.domain.model.RoleName;

public interface RoleCatalogPort {
    boolean existsByName(RoleName roleName);
}

