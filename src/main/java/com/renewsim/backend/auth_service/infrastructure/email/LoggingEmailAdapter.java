package com.renewsim.backend.auth_service.infrastructure.email;

import com.renewsim.backend.auth_service.application.port.out.EmailPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * No-op email adapter for local development and automated tests.
 *
 * <p>
 * Active on profile {@code test}. Instead of sending real emails it writes
 * a clearly-marked log line at WARN level so developers can copy verification
 * and reset links directly from the console.
 *
 * <p>
 * <strong>Security rule:</strong> this adapter intentionally prints
 * sensitive data (verification tokens, reset tokens) to the console. It MUST
 * NEVER be activated on {@code docker} or {@code prod} profiles — enforced
 * by the {@code @Profile} annotation.
 *
 * <p>
 * Trade-off accepted: logging stub is simpler than a Greenmail/MockSMTP
 * setup and sufficient for the thesis scope. A real {@code SmtpEmailAdapter}
 * bound to {@code docker|prod} replaces this before going live.
 */
@Slf4j
@Component
@Profile({ "test", "local" })
public class LoggingEmailAdapter implements EmailPort {

    private static final String SEP = "=".repeat(60);

    @Override
    public void sendVerificationEmail(String toEmail, String username, String verificationToken) {
        validateNotBlank(toEmail, "toEmail");
        validateNotBlank(username, "username");
        validateNotBlank(verificationToken, "verificationToken");

        String url = "http://localhost:3000/verify-email?token=" + verificationToken;

        log.warn("""
                {}
                [EMAIL STUB — test only]  sendVerificationEmail
                To            : {}
                Username      : {}
                Verification  : {}
                {}""",
                SEP, toEmail, username, url, SEP);
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String username, String resetToken) {
        validateNotBlank(toEmail, "toEmail");
        validateNotBlank(username, "username");
        validateNotBlank(resetToken, "resetToken");

        String url = "http://localhost:3000/reset-password?token=" + resetToken;

        log.warn("""
                {}
                [EMAIL STUB — test only]  sendPasswordResetEmail
                To            : {}
                Username      : {}
                Reset URL     : {}
                {}""",
                SEP, toEmail, username, url, SEP);
    }

    private static void validateNotBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be null or blank");
        }
    }
}