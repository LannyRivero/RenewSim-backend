package com.renewsim.backend.auth_service.infrastructure.email;

import com.renewsim.backend.auth_service.application.port.out.EmailPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * No-op email adapter for local development and automated tests.
 *
 * <p>
 * Active on profiles {@code local} and {@code test}. Instead of sending
 * real emails it writes a clearly-marked log line at WARN level so developers
 * can copy OTPs and activation links directly from the console.
 *
 * <p>
 * <strong>Security rule:</strong> this adapter intentionally prints
 * sensitive data (raw OTPs, activation tokens) to the console. It must
 * <em>never</em> be activated on {@code docker} or {@code prod} profiles,
 * which is enforced by the {@code @Profile} annotation.
 *
 * <p>
 * Trade-off: a logging adapter is simpler than a Greenmail/mock-SMTP
 * setup and is sufficient for the thesis scope. A real {@code SmtpEmailAdapter}
 * bound to {@code docker|prod} must replace this before going live.
 */
@Slf4j
@Component
@Profile({ "local", "test" })
public class LoggingEmailAdapter implements EmailPort {

    private static final String SEPARATOR = "=".repeat(60);

    @Override
    public void sendOtp(String toEmail, String rawOtp, int expiresInSeconds) {
        validateNotBlank(toEmail, "toEmail");
        validateNotBlank(rawOtp, "rawOtp");

        log.warn("""
                {}
                [EMAIL STUB — local/test only]  sendOtp
                To            : {}
                OTP           : {}
                Expires in    : {} seconds
                {}""",
                SEPARATOR, toEmail, rawOtp, expiresInSeconds, SEPARATOR);
    }

    @Override
    public void sendActivationEmail(String toEmail, String activationToken) {
        validateNotBlank(toEmail, "toEmail");
        validateNotBlank(activationToken, "activationToken");

        String activationUrl = "http://localhost:8080/api/v1/auth/activate?token=" + activationToken;

        log.warn("""
                {}
                [EMAIL STUB — local/test only]  sendActivationEmail
                To            : {}
                Activation URL: {}
                {}""",
                SEPARATOR, toEmail, activationUrl, SEPARATOR);
    }

    // ─────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────

    private static void validateNotBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be null or blank");
        }
    }
}