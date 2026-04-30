package com.renewsim.backend.auth_service.application.service;

import com.renewsim.backend.auth_service.application.port.out.EmailVerificationTokenRepository;
import com.renewsim.backend.auth_service.domain.model.EmailVerificationToken;
import com.renewsim.backend.user_service.application.port.out.UserRepositoryPort;
import com.renewsim.backend.user_service.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for verifying user email addresses.
 * 
 * Business rules:
 * - Token must exist
 * - Token must not be expired
 * - Token must not have been used already
 * - User must exist
 * - Sets user.emailVerified = true
 * - Marks token as verified
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerifyEmailUseCase {

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepositoryPort userRepository;

    @Transactional
    public void execute(String token) {
        log.debug("Attempting to verify email with token");

        // Find token
        EmailVerificationToken verificationToken = tokenRepository.findByToken(token)
            .orElseThrow(() -> new EmailVerificationException("Invalid or expired verification token"));

        // Validate token can be used
        try {
            verificationToken.validateCanBeUsed();
        } catch (IllegalStateException e) {
            throw new EmailVerificationException(e.getMessage());
        }

        // Find user
        User user = userRepository.findById(verificationToken.getUserId())
            .orElseThrow(() -> new EmailVerificationException("User not found"));

        // Verify email (idempotent)
        user.verifyEmail();
        userRepository.save(user);

        // Mark token as verified
        verificationToken.markAsVerified();
        tokenRepository.save(verificationToken);

        log.info("Email verified successfully for user={}", user.getId());
    }

    /**
     * Exception thrown when email verification fails.
     */
    public static class EmailVerificationException extends RuntimeException {
        public EmailVerificationException(String message) {
            super(message);
        }
    }
}