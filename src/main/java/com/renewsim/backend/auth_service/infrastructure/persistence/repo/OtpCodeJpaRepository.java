package com.renewsim.backend.auth_service.infrastructure.persistence.repo;

import com.renewsim.backend.auth_service.domain.model.OtpCode;
import com.renewsim.backend.auth_service.infrastructure.persistence.entity.OtpCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OtpCodeJpaRepository extends JpaRepository<OtpCodeEntity, Long> {

    @Query("""
            SELECT o FROM OtpCodeEntity o
            WHERE o.userId = :userId
              AND o.purpose = :purpose
              AND o.verifiedAt IS NULL
              AND o.expiresAt > CURRENT_TIMESTAMP
            ORDER BY o.issuedAt DESC
            LIMIT 1
            """)
    Optional<OtpCodeEntity> findLatestValidByUserIdAndPurpose(
            @Param("userId") Long userId,
            @Param("purpose") OtpCode.Purpose purpose);

    @Modifying
    @Query("""
            UPDATE OtpCodeEntity o
            SET o.verifiedAt = CURRENT_TIMESTAMP
            WHERE o.userId = :userId
              AND o.purpose = :purpose
              AND o.verifiedAt IS NULL
            """)
    void invalidateAllByUserIdAndPurpose(
            @Param("userId") Long userId,
            @Param("purpose") OtpCode.Purpose purpose);
}