package com.renewsim.backend.auth_service.infrastructure.persistence.repo;

import com.renewsim.backend.auth_service.infrastructure.persistence.entity.TokenBlacklistEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenBlacklistJpaRepository extends JpaRepository<TokenBlacklistEntity, Long> {

    boolean existsByJti(String jti);
}