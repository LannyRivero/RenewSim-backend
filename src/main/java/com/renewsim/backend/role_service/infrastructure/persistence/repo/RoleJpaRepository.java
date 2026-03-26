package com.renewsim.backend.role_service.infrastructure.persistence.repo;

import com.renewsim.backend.shared.domain.vo.RoleName;
import com.renewsim.backend.role_service.infrastructure.persistence.entity.RoleEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleJpaRepository extends JpaRepository<RoleEntity, Long> {
    
    Optional<RoleEntity> findByName(RoleName name);
    
    long countByName(RoleName name);
}

