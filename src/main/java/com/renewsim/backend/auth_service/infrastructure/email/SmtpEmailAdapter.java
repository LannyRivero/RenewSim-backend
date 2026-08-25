package com.renewsim.backend.auth_service.infrastructure.email;

import com.renewsim.backend.auth_service.application.port.out.EmailPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * SMTP email adapter reserved for the explicit {@code smtp} profile.
 *
 * <p>
 * <strong>Status: STUB — NOT YET IMPLEMENTED (Phase 5 / D2-01-smtp)</strong>
 *
 * <p>
 * This class satisfies Spring's bean requirement on non-local profiles.
 * All methods throw {@link UnsupportedOperationException} until a real
 * SMTP / SES implementation is provided.
 *
 * <p>
 * Implementation checklist (Phase 5):
 * <ol>
 * <li>Add {@code spring-boot-starter-mail} in pom.xml.</li>
 * <li>Inject {@code JavaMailSender} and a template engine.</li>
 * <li>Configure {@code spring.mail.*} via env vars — never hardcode.</li>
 * <li>Replace {@link UnsupportedOperationException} with real send logic.</li>
 * <li>Add Resilience4j retry around {@code mailSender.send()}.</li>
 * <li>Write integration tests with GreenMail.</li>
 * </ol>
 *
 * <p>
 * <strong>Security rule:</strong> never log raw verification or reset tokens —
 * log only the recipient address and a masked reference.
 */
@Slf4j
@Component
@Profile("smtp")
public class SmtpEmailAdapter implements EmailPort {

    @Override
    public void sendVerificationEmail(String toEmail, String username, String verificationToken) {
        log.error("SmtpEmailAdapter.sendVerificationEmail() not implemented. Email NOT sent to={}", toEmail);
        throw new UnsupportedOperationException(
                "SmtpEmailAdapter is not implemented yet. See D2-01-smtp in the backlog.");
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String username, String resetToken) {
        log.error("SmtpEmailAdapter.sendPasswordResetEmail() not implemented. Email NOT sent to={}", toEmail);
        throw new UnsupportedOperationException(
                "SmtpEmailAdapter is not implemented yet. See D2-01-smtp in the backlog.");
    }
}
