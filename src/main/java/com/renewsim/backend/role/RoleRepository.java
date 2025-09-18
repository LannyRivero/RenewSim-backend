package com.renewsim.backend.role;

import org.springframework.data.jpa.repository.JpaRepository;

import com.renewsim.backend.role_service.domain.model.RoleName;

import java.util.Optional;


public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}


