package com.renewsim.backend.user_service.application.port.out;

import java.util.Optional;

import com.renewsim.backend.role_service.domain.model.RoleName;
import com.renewsim.backend.user_service.dto.RoleSnapshot;

public interface RoleCatalogPort {
    boolean existsByName(RoleName roleName);
   Optional<RoleSnapshot> findByName(String name);
}

