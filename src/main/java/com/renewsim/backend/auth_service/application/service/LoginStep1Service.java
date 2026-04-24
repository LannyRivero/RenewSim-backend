package com.renewsim.backend.auth_service.application.service;

import com.renewsim.backend.auth_service.application.command.LoginStep1Command;
import com.renewsim.backend.auth_service.application.dto.UserSnapshot;
import com.renewsim.backend.auth_service.application.port.in.LoginStep1UseCase;
import com.renewsim.backend.auth_service.application.port.out.EmailPort;
import com.renewsim.backend.auth_service.application.port.out.OtpCodeRepositoryPort;
import com.renewsim.backend.auth_service.application.port.out.PasswordEncoderPort;
import com.renewsim.backend.auth_service.application.port.out.TransactionalPort;
import com.renewsim.backend.auth_service.application.port.out.UserAccountGateway;
import com.renewsim.backend.auth_service.application.result.LoginStep1Result;
import com.renewsim.backend.auth_service.application.validator.CredentialsValidator;
import com.renewsim.backend.auth_service.application.validator.UserAccountValidator;
import com.renewsim.backend.auth_service.domain.model.OtpCode;
import com.renewsim.backend.auth_service.domain.service.OtpGenerator;
import com.renewsim.backend.shared.exception.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;

/**
 * Implementación del caso de uso de autenticación - Paso 1.
 *
 * Valida credenciales básicas (email + password) y genera un OTP
 * que será enviado al email del usuario. Diseñado para prevenir
 * ataques de enumeración de usuarios mediante respuestas genéricas.
 *
 * @since 1.0.0
 */
public class LoginStep1Service implements LoginStep1UseCase {

    private static final Logger log = LoggerFactory.getLogger(LoginStep1Service.class);
    private static final int OTP_EXPIRES_IN_SECONDS = 300;

    private final UserAccountGateway userAccountGateway;
    private final OtpCodeRepositoryPort otpCodeRepositoryPort;
    private final OtpGenerator otpGenerator;
    private final CredentialsValidator credentialsValidator;
    private final PasswordEncoderPort passwordEncoder;
    private final EmailPort emailPort;
    private final TransactionalPort transactionalPort;
    private final Clock clock;
    private final UserAccountValidator userAccountValidator;

    public LoginStep1Service(
            UserAccountGateway userAccountGateway,
            OtpCodeRepositoryPort otpCodeRepositoryPort,
            OtpGenerator otpGenerator,
            CredentialsValidator credentialsValidator,
            PasswordEncoderPort passwordEncoder,
            EmailPort emailPort,
            TransactionalPort transactionalPort,
            Clock clock,
            UserAccountValidator userAccountValidator) {
        this.userAccountGateway = userAccountGateway;
        this.otpCodeRepositoryPort = otpCodeRepositoryPort;
        this.otpGenerator = otpGenerator;
        this.credentialsValidator = credentialsValidator;
        this.passwordEncoder = passwordEncoder;
        this.emailPort = emailPort;
        this.transactionalPort = transactionalPort;
        this.clock = clock;
        this.userAccountValidator = userAccountValidator;
    }

    @Override
    public LoginStep1Result execute(LoginStep1Command command) {
        return transactionalPort.execute(() -> executeInternal(command));
    }

    private LoginStep1Result executeInternal(LoginStep1Command command) {
        // Intentionally generic response — never reveal whether email exists
        UserSnapshot user = userAccountGateway.findByEmail(command.email())
                .orElse(null);

        if (user == null || !userAccountValidator.isEnabled(user)) {
            log.warn("Login step1 attempted for unknown or disabled email");
            return genericResponse();
        }

        // Intentionally generic response on wrong password — no timing/enumeration leak
        try {
            credentialsValidator.validatePassword(command.password(), user.passwordHash());
        } catch (AuthenticationException e) {
            log.warn("Login step1 invalid password for userId={}", user.id());
            return genericResponse();
        }

        // Invalidate any previous OTP for this user
        otpCodeRepositoryPort.invalidateAllByUserId(user.id(), OtpCode.Purpose.LOGIN);

        // Generate and persist new OTP
        String rawOtp = otpGenerator.generate();
        String hashedOtp = passwordEncoder.encode(rawOtp);
        OtpCode otpCode = OtpCode.issue(user.id(), hashedOtp, OtpCode.Purpose.LOGIN, clock);
        otpCodeRepositoryPort.save(otpCode);

        // Deliver OTP via email — adapter is profile-specific
        emailPort.sendOtp(user.email(), rawOtp, OTP_EXPIRES_IN_SECONDS);
        log.info("OTP sent for userId={}", user.id());

        return genericResponse();
    }

    private static LoginStep1Result genericResponse() {
        return new LoginStep1Result(
                "If your account exists and is active, you will receive an OTP.",
                OTP_EXPIRES_IN_SECONDS);
    }
}