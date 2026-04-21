package com.renewsim.backend.auth_service.infrastructure.persistence.repo;

import com.renewsim.backend.auth_service.infrastructure.persistence.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenEntity, Long> {

    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

    @Modifying
    @Query("""
            UPDATE RefreshTokenEntity r
            SET r.revoked = true, r.revokedAt = CURRENT_TIMESTAMP
            WHERE r.userId = :userId
              AND r.revoked = false
            """)
    void revokeAllByUserId(@Param("userId") Long userId);
}