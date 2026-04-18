package com.renewsim.backend.auth_service.infrastructure.persistence.repo;

import com.renewsim.backend.auth_service.infrastructure.persistence.entity.ActivationTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ActivationTokenJpaRepository extends JpaRepository<ActivationTokenEntity, Long> {

    Optional<ActivationTokenEntity> findByTokenHash(String tokenHash);
}