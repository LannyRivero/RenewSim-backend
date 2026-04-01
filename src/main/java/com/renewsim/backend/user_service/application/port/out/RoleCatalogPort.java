package com.renewsim.backend.user_service.application.port.out;

import java.util.Optional;

import com.renewsim.backend.shared.domain.vo.RoleName;
import com.renewsim.backend.user_service.web.dto.RoleSnapshot;

public interface RoleCatalogPort {
    boolean existsByName(RoleName roleName);
   Optional<RoleSnapshot> findByName(String name);
}

