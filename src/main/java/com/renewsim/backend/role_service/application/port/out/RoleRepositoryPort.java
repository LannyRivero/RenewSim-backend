package com.renewsim.backend.role_service.application.port.out;

import com.renewsim.backend.role_service.domain.model.Role;
import com.renewsim.backend.role_service.domain.model.RoleName;
import java.util.Optional;

public interface RoleRepositoryPort {
    Optional<Role> findByName(RoleName roleName);
    Role save(Role role);
}
