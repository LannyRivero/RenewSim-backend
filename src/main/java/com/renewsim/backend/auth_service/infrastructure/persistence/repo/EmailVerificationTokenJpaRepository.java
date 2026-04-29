package com.renewsim.backend.auth_service.infrastructure.persistence.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.renewsim.backend.auth_service.infrastructure.persistence.entity.EmailVerificationTokenEntity;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * JPA repository for email verification tokens.
 */
@Repository
public interface EmailVerificationTokenJpaRepository extends JpaRepository<EmailVerificationTokenEntity, Long> {

    /**
     * Find a token by its value.
     *
     * @param token the token string
     * @return Optional containing the token entity if found
     */
    Optional<EmailVerificationTokenEntity> findByToken(String token);

    /**
     * Find all tokens for a specific user.
     *
     * @param userId the user ID
     * @return Optional containing the most recent token
     */
    Optional<EmailVerificationTokenEntity> findFirstByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Delete all expired and verified tokens (cleanup task).
     *
     * @param expirationDate tokens created before this date
     * @return number of deleted tokens
     */
    @Modifying
    @Query("DELETE FROM EmailVerificationTokenEntity e WHERE e.expiresAt < :expirationDate AND e.verifiedAt IS NOT NULL")
    int deleteExpiredAndVerifiedTokens(@Param("expirationDate") LocalDateTime expirationDate);

    /**
     * Check if a valid (non-expired, non-verified) token exists for a user.
     *
     * @param userId the user ID
     * @param now current timestamp
     * @return true if a valid token exists
     */
    @Query("SELECT COUNT(e) > 0 FROM EmailVerificationTokenEntity e WHERE e.userId = :userId AND e.expiresAt > :now AND e.verifiedAt IS NULL")
    boolean existsValidTokenForUser(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}