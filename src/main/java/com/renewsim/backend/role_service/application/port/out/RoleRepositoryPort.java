package com.renewsim.backend.role_service.application.port.out;

import com.renewsim.backend.role_service.domain.model.Role;
import com.renewsim.backend.role_service.domain.model.RoleName;

import java.util.List;
import java.util.Optional;

public interface RoleRepositoryPort {

    Role save(Role role);

    Optional<Role> findById(Long id);

    Optional<Role> findByName(RoleName roleName);

    List<Role> findAll(); 

    void deleteById(Long id);
    long countByName(RoleName roleName);
}

