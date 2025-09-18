package com.renewsim.backend.role_service.application.port.out;

import com.renewsim.backend.role_service.domain.model.Role;
import com.renewsim.backend.role_service.domain.model.RoleName;
import java.util.List;
import java.util.Optional;

public interface RoleRepositoryPort {
    Role save(Role role);

    List<Role> findAll();

    Optional<Role> findByName(RoleName roleName);

    Optional<Role> findById(Long id);

    void deleteById(Long roleId);
}
