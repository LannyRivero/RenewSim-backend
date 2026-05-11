package com.renewsim.backend.auth_service.application.service;

import com.renewsim.backend.auth_service.application.port.out.EmailVerificationTokenRepository;
import com.renewsim.backend.auth_service.application.port.out.UserAccountGateway;
import com.renewsim.backend.auth_service.domain.model.EmailVerificationToken;
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
 * - Activates the user account via UserAccountGateway (auth → user_service
 * boundary)
 * - Marks token as verified
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerifyEmailUseCase {

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserAccountGateway userAccountGateway;

    @Transactional
    public void execute(String token) {
        log.debug("Attempting to verify email with token");

        EmailVerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new EmailVerificationException("Invalid or expired verification token"));

        try {
            verificationToken.validateCanBeUsed();
        } catch (IllegalStateException e) {
            throw new EmailVerificationException(e.getMessage());
        }

        userAccountGateway.activateUser(verificationToken.getUserId());

        verificationToken.markAsVerified();
        tokenRepository.save(verificationToken);

        log.info("Email verified successfully for userId={}", verificationToken.getUserId());
    }

    public static class EmailVerificationException extends RuntimeException {
        public EmailVerificationException(String message) {
            super(message);
        }
    }
}