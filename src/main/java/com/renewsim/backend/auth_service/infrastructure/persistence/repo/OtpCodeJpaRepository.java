package com.renewsim.backend.auth_service.infrastructure.persistence.repo;

import com.renewsim.backend.auth_service.domain.model.OtpCode;
import com.renewsim.backend.auth_service.infrastructure.persistence.entity.OtpCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface OtpCodeJpaRepository extends JpaRepository<OtpCodeEntity, Long> {

        Optional<OtpCodeEntity> findFirstByUserIdAndPurposeAndVerifiedAtIsNullAndExpiresAtAfterOrderByIssuedAtDesc(
                        Long userId,
                        OtpCode.Purpose purpose,
                        Instant now);

        @Modifying
        @Query("""
                        UPDATE OtpCodeEntity o
                        SET o.verifiedAt = :verifiedAt
                        WHERE o.userId = :userId
                          AND o.purpose = :purpose
                          AND o.verifiedAt IS NULL
                        """)
        void invalidateAllByUserIdAndPurpose(
                        @Param("userId") Long userId,
                        @Param("purpose") OtpCode.Purpose purpose,
                        @Param("verifiedAt") Instant verifiedAt);

        @Query("""
                        SELECT COUNT(o)
                        FROM OtpCodeEntity o
                        WHERE o.userId = :userId
                          AND o.purpose = :purpose
                          AND o.verifiedAt IS NULL
                          AND o.expiresAt > :now
                        """)
        long countFailedAttempts(
                        @Param("userId") Long userId,
                        @Param("purpose") OtpCode.Purpose purpose,
                        @Param("now") Instant now);
}