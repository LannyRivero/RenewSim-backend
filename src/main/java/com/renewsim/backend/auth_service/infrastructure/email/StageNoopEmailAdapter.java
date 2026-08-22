package com.renewsim.backend.auth_service.infrastructure.email;

import com.renewsim.backend.auth_service.application.port.out.EmailPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Stage/showcase adapter that intentionally avoids delivering or logging raw tokens.
 *
 * <p>For the first public demo we rely on seeded accounts and JWT login rather than
 * email-driven verification flows. This adapter keeps the application context complete
 * on the {@code stage} profile without exposing verification or reset tokens in logs.</p>
 */
@Slf4j
@Component
@Profile("stage")
public class StageNoopEmailAdapter implements EmailPort {

    @Override
    public void sendVerificationEmail(String toEmail, String username, String verificationToken) {
        log.warn("StageNoopEmailAdapter skipped verification email delivery to={}", maskEmail(toEmail));
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String username, String resetToken) {
        log.warn("StageNoopEmailAdapter skipped password reset email delivery to={}", maskEmail(toEmail));
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        int at = email.indexOf('@');
        return at <= 2 ? "***" + email.substring(at) : email.substring(0, 2) + "***" + email.substring(at);
    }
}
