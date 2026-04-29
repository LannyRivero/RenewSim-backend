package com.renewsim.backend.auth_service.application.service;

import com.renewsim.backend.auth_service.application.port.out.EmailPort;
import com.renewsim.backend.auth_service.application.port.out.EmailVerificationTokenRepository;
import com.renewsim.backend.auth_service.domain.model.EmailVerificationToken;
import com.renewsim.backend.user_service.application.port.out.UserRepositoryPort;
import com.renewsim.backend.user_service.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

/**
 * Use case for resending email verification link.
 * 
 * Business rules:
 * - User must exist
 * - User must not already be verified
 * - Generates new token (invalidates previous)
 * - Sends verification email
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ResendVerificationEmailUseCase {

    private final UserRepositoryPort userRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final EmailPort emailPort;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.email.verification.expiration-hours:48}")
    private int expirationHours;

    @Transactional
    public void execute(String email) {
        log.debug("Resending verification email to={}", maskEmail(email));

        // Find user by email
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResendVerificationException("User not found"));

        // Check if already verified
        if (user.isEmailVerified()) {
            throw new ResendVerificationException("Email already verified");
        }

        // Generate new token
        String token = generateSecureToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(expirationHours);

        EmailVerificationToken verificationToken = new EmailVerificationToken(
            user.getId(),
            token,
            expiresAt
        );

        tokenRepository.save(verificationToken);

        // Send email
        emailPort.sendVerificationEmail(user.getEmail(), user.getFullName(), token);

        log.info("Verification email resent to user={}", user.getId());
    }

    private String generateSecureToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        int at = email.indexOf("@");
        return at <= 2 ? "***" + email.substring(at) : email.substring(0, 2) + "***" + email.substring(at);
    }

    /**
     * Exception thrown when resend verification fails.
     */
    public static class ResendVerificationException extends RuntimeException {
        public ResendVerificationException(String message) {
            super(message);
        }
    }
}