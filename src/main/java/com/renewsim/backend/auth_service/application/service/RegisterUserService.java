package com.renewsim.backend.auth_service.application.service;

import com.renewsim.backend.auth_service.application.command.RegisterCommand;
import com.renewsim.backend.auth_service.application.port.in.RegisterUserUseCase;
import com.renewsim.backend.auth_service.application.port.out.ActivationTokenRepositoryPort;
import com.renewsim.backend.auth_service.application.port.out.EmailPort;
import com.renewsim.backend.auth_service.application.port.out.PasswordEncoderPort;
import com.renewsim.backend.auth_service.application.port.out.TransactionalPort;
import com.renewsim.backend.auth_service.application.port.out.UserAccountGateway;
import com.renewsim.backend.auth_service.application.result.RegisterResult;
import com.renewsim.backend.auth_service.domain.model.ActivationToken;
import com.renewsim.backend.auth_service.domain.model.AuthUserStatus;
import com.renewsim.backend.auth_service.domain.service.OtpGenerator;
import com.renewsim.backend.shared.domain.vo.RoleName;
import com.renewsim.backend.shared.exception.ConflictException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.util.Set;

public class RegisterUserService implements RegisterUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(RegisterUserService.class);
    private static final Set<RoleName> DEFAULT_ROLES = Set.of(RoleName.USER);

    private final UserAccountGateway userAccountGateway;
    private final ActivationTokenRepositoryPort activationTokenRepositoryPort;
    private final EmailPort emailPort;
    private final TransactionalPort transactionalPort;
    private final PasswordEncoderPort passwordEncoderPort;
    private final OtpGenerator otpGenerator;
    private final Clock clock;

    public RegisterUserService(
            UserAccountGateway userAccountGateway,
            ActivationTokenRepositoryPort activationTokenRepositoryPort,
            EmailPort emailPort,
            TransactionalPort transactionalPort,
            PasswordEncoderPort passwordEncoderPort,
            OtpGenerator otpGenerator,
            Clock clock) {
        this.userAccountGateway = userAccountGateway;
        this.activationTokenRepositoryPort = activationTokenRepositoryPort;
        this.emailPort = emailPort;
        this.transactionalPort = transactionalPort;
        this.passwordEncoderPort = passwordEncoderPort;
        this.otpGenerator = otpGenerator;
        this.clock = clock;
    }

    @Override
    public RegisterResult execute(RegisterCommand command) {
        return transactionalPort.execute(() -> executeInternal(command));
    }

    private RegisterResult executeInternal(RegisterCommand command) {
        if (userAccountGateway.existsByEmail(command.email())) {
            log.warn("Registration failed: email already exists email={}", command.email());
            throw new ConflictException("Email already registered");
        }

        String rawToken = otpGenerator.generate();
        String tokenHash = passwordEncoderPort.encode(rawToken);
        String username = extractUsernameFromEmail(command.email());

        var userSnapshot = userAccountGateway.createUser(
                username,
                command.fullName(),
                command.password(),
                command.email(),
                DEFAULT_ROLES
        );

        ActivationToken activationToken = ActivationToken.issue(
                userSnapshot.id(),
                tokenHash,
                clock
        );
        activationTokenRepositoryPort.save(activationToken);

        emailPort.sendActivationEmail(command.email(), rawToken);
        log.info("User registered with activation token userId={}", userSnapshot.id());

        return new RegisterResult(
                userSnapshot.id(),
                userSnapshot.email(),
                userSnapshot.fullName(),
                AuthUserStatus.INACTIVE,
                "Registration successful. Please check your email to activate your account."
        );
    }

    private String extractUsernameFromEmail(String email) {
        int atIndex = email.indexOf('@');
        return atIndex > 0 ? email.substring(0, atIndex) : email;
    }
}