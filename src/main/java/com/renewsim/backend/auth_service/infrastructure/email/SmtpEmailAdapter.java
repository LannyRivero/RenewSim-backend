package com.renewsim.backend.auth_service.infrastructure.email;

import com.renewsim.backend.auth_service.application.port.out.EmailPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * SMTP email adapter for {@code docker} and {@code prod} profiles.
 *
 * <p>
 * <strong>Status: STUB — NOT YET IMPLEMENTED (Phase 5 / D2-01-smtp)</strong>
 *
 * <p>
 * This class is the placeholder that satisfies Spring's bean requirement
 * on non-local profiles. All methods throw
 * {@link UnsupportedOperationException}
 * until a real SMTP / SES implementation is provided.
 *
 * <p>
 * Implementation checklist (Phase 5):
 * <ol>
 * <li>Add {@code spring-boot-starter-mail} dependency in pom.xml.</li>
 * <li>Inject {@code JavaMailSender} and a {@code TemplateEngine} (Thymeleaf
 * or FreeMarker) for HTML templates.</li>
 * <li>Configure {@code spring.mail.*} properties in
 * {@code application-docker.yml} and {@code application-prod.yml}
 * using environment variables — never hardcode credentials.</li>
 * <li>Replace the {@link UnsupportedOperationException} calls with real
 * {@code MimeMessage} construction and {@code mailSender.send()} calls.</li>
 * <li>Add Resilience4j retry around {@code mailSender.send()} to handle
 * transient SMTP failures.</li>
 * <li>Write integration tests with GreenMail or MockMvc mail capture.</li>
 * </ol>
 *
 * <p>
 * <strong>Security rule:</strong> never log raw OTPs or activation tokens
 * in this adapter — log only {@code to} address and a masked reference.
 */
@Slf4j
@Component
@Profile({ "docker", "prod" })
public class SmtpEmailAdapter implements EmailPort {

    @Override
    public void sendOtp(String toEmail, String rawOtp, int expiresInSeconds) {
        // TODO Phase 5 — implement SMTP delivery
        log.error("SmtpEmailAdapter.sendOtp() is not implemented. Email NOT sent to={}", toEmail);
        throw new UnsupportedOperationException(
                "SmtpEmailAdapter is not implemented yet. See D2-01-smtp in the backlog.");
    }

    @Override
    public void sendActivationEmail(String toEmail, String activationToken) {
        // TODO Phase 5 — implement SMTP delivery
        log.error("SmtpEmailAdapter.sendActivationEmail() is not implemented. Email NOT sent to={}", toEmail);
        throw new UnsupportedOperationException(
                "SmtpEmailAdapter is not implemented yet. See D2-01-smtp in the backlog.");
    }
}