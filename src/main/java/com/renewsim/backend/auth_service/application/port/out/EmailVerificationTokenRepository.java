package com.renewsim.backend.auth_service.application.port.out;

import com.renewsim.backend.auth_service.domain.model.EmailVerificationToken;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Repository port for email verification tokens.
 * Defines domain operations, implemented by infrastructure layer.
 */
public interface EmailVerificationTokenRepository {

    /**
     * Save a new verification token or update existing one.
     *
     * @param token the token to save
     * @return the saved token with generated ID
     */
    EmailVerificationToken save(EmailVerificationToken token);

    /**
     * Find a token by its value.
     *
     * @param token the token string
     * @return Optional containing the token if found
     */
    Optional<EmailVerificationToken> findByToken(String token);

    /**
     * Find the most recent token for a user.
     *
     * @param userId the user ID
     * @return Optional containing the most recent token
     */
    Optional<EmailVerificationToken> findLatestByUserId(Long userId);

    /**
     * Check if a valid (non-expired, non-verified) token exists for a user.
     *
     * @param userId the user ID
     * @return true if a valid token exists
     */
    boolean existsValidTokenForUser(Long userId);

    /**
     * Delete expired and verified tokens (cleanup operation).
     *
     * @param before delete tokens created before this date
     * @return number of deleted tokens
     */
    int deleteExpiredAndVerifiedTokens(LocalDateTime before);
}