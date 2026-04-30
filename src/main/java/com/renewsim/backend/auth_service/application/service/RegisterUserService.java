package com.renewsim.backend.auth_service.application.service;

import com.renewsim.backend.auth_service.application.command.RegisterCommand;
import com.renewsim.backend.auth_service.application.port.in.RegisterUserUseCase;
import com.renewsim.backend.auth_service.application.port.out.EmailPort;
import com.renewsim.backend.auth_service.application.port.out.EmailVerificationTokenRepository;
import com.renewsim.backend.auth_service.application.port.out.TransactionalPort;
import com.renewsim.backend.auth_service.application.port.out.UserAccountGateway;
import com.renewsim.backend.auth_service.application.result.RegisterResult;
import com.renewsim.backend.auth_service.domain.model.AuthUserStatus;
import com.renewsim.backend.auth_service.domain.model.EmailVerificationToken;
import com.renewsim.backend.shared.domain.vo.RoleName;
import com.renewsim.backend.shared.exception.ConflictException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Set;

public class RegisterUserService implements RegisterUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(RegisterUserService.class);
    private static final Set<RoleName> DEFAULT_ROLES = Set.of(RoleName.USER);

    private final UserAccountGateway userAccountGateway;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final EmailPort emailPort;
    private final TransactionalPort transactionalPort;
    private final SecureRandom secureRandom;
    private final int verificationExpirationHours;

    public RegisterUserService(
            UserAccountGateway userAccountGateway,
            EmailVerificationTokenRepository emailVerificationTokenRepository,
            EmailPort emailPort,
            TransactionalPort transactionalPort,
            @Value("${app.email.verification.expiration-hours:48}") int verificationExpirationHours) {
        this.userAccountGateway = userAccountGateway;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.emailPort = emailPort;
        this.transactionalPort = transactionalPort;
        this.secureRandom = new SecureRandom();
        this.verificationExpirationHours = verificationExpirationHours;
    }

    @Override
    public RegisterResult execute(RegisterCommand command) {
        return transactionalPort.execute(() -> executeInternal(command));
    }

    private RegisterResult executeInternal(RegisterCommand command) {
        if (userAccountGateway.existsByEmail(command.email())) {
            log.warn("Registration failed: email already exists email={}", maskEmail(command.email()));
            throw new ConflictException("Email already registered");
        }

        String username = extractUsernameFromEmail(command.email());

        var userSnapshot = userAccountGateway.createUser(
                username,
                command.fullName(),
                command.password(),
                command.email(),
                DEFAULT_ROLES
        );

        // Generate email verification token
        String verificationToken = generateSecureToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(verificationExpirationHours);
        
        EmailVerificationToken emailVerificationToken = new EmailVerificationToken(
                userSnapshot.id(),
                verificationToken,
                expiresAt
        );
        emailVerificationTokenRepository.save(emailVerificationToken);

        // Send verification email
        emailPort.sendVerificationEmail(command.email(), command.fullName(), verificationToken);

        log.info("User registered successfully. Verification email sent. userId={}", userSnapshot.id());

        return new RegisterResult(
                userSnapshot.id(),
                userSnapshot.email(),
                userSnapshot.fullName(),
                AuthUserStatus.INACTIVE,
                "Registration successful. Please check your email to verify your account."
        );
    }

    private String generateSecureToken() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    private String extractUsernameFromEmail(String email) {
        int atIndex = email.indexOf('@');
        return atIndex > 0 ? email.substring(0, atIndex) : email;
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        int at = email.indexOf("@");
        return at <= 2 ? "***" + email.substring(at) : email.substring(0, 2) + "***" + email.substring(at);
    }
}