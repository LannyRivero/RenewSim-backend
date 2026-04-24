package com.renewsim.backend.auth_service.application.service;

import com.renewsim.backend.auth_service.application.command.LoginStep2Command;
import com.renewsim.backend.auth_service.application.dto.UserSnapshot;
import com.renewsim.backend.auth_service.application.port.in.LoginStep2UseCase;
import com.renewsim.backend.auth_service.application.port.out.OtpCodeRepositoryPort;
import com.renewsim.backend.auth_service.application.port.out.PasswordEncoderPort;
import com.renewsim.backend.auth_service.application.port.out.RefreshTokenRepositoryPort;
import com.renewsim.backend.auth_service.application.port.out.TokenProvider;
import com.renewsim.backend.auth_service.application.port.out.TransactionalPort;
import com.renewsim.backend.auth_service.application.port.out.UserAccountGateway;
import com.renewsim.backend.auth_service.application.result.LoginStep2Result;
import com.renewsim.backend.auth_service.application.validator.UserAccountValidator;
import com.renewsim.backend.auth_service.domain.AuthenticatedUser;
import com.renewsim.backend.auth_service.domain.model.OtpCode;
import com.renewsim.backend.auth_service.domain.model.RefreshToken;
import com.renewsim.backend.auth_service.domain.service.TokenHasher;
import com.renewsim.backend.shared.exception.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementación del caso de uso de autenticación - Paso 2.
 *
 * Valida el código OTP proporcionado y genera tokens de acceso
 * y refresh para el usuario autenticado.
 *
 * @since 1.0.0
 */
public class LoginStep2Service implements LoginStep2UseCase {

    private static final Logger log = LoggerFactory.getLogger(LoginStep2Service.class);

    private final UserAccountGateway userAccountGateway;
    private final OtpCodeRepositoryPort otpCodeRepositoryPort;
    private final RefreshTokenRepositoryPort refreshTokenRepositoryPort;
    private final TokenProvider tokenProvider;
    private final PasswordEncoderPort passwordEncoderPort;
    private final TransactionalPort transactionalPort;
    private final Clock clock;
    private final UserAccountValidator userAccountValidator;

    public LoginStep2Service(
            UserAccountGateway userAccountGateway,
            OtpCodeRepositoryPort otpCodeRepositoryPort,
            RefreshTokenRepositoryPort refreshTokenRepositoryPort,
            TokenProvider tokenProvider,
            PasswordEncoderPort passwordEncoderPort,
            TransactionalPort transactionalPort,
            Clock clock,
            UserAccountValidator userAccountValidator) {
        this.userAccountGateway = userAccountGateway;
        this.otpCodeRepositoryPort = otpCodeRepositoryPort;
        this.refreshTokenRepositoryPort = refreshTokenRepositoryPort;
        this.tokenProvider = tokenProvider;
        this.passwordEncoderPort = passwordEncoderPort;
        this.transactionalPort = transactionalPort;
        this.clock = clock;
        this.userAccountValidator = userAccountValidator;
    }

    @Override
    public LoginStep2Result execute(LoginStep2Command command) {
        return transactionalPort.execute(() -> executeInternal(command));
    }

    private LoginStep2Result executeInternal(LoginStep2Command command) {
        UserSnapshot user = userAccountGateway.findByEmail(command.email())
                .orElseThrow(() -> new AuthenticationException("Invalid credentials"));

        userAccountValidator.validateEnabledOrThrow(user);

        OtpCode otpCode = otpCodeRepositoryPort
                .findLatestValidByUserId(user.id(), OtpCode.Purpose.LOGIN)
                .orElseThrow(() -> new AuthenticationException("Invalid or expired OTP"));

        if (!otpCode.isValid()) {
            throw new AuthenticationException("Invalid or expired OTP");
        }

        if (!passwordEncoderPort.matches(command.otpCode(), otpCode.getCodeHash())) {
            throw new AuthenticationException("Invalid or expired OTP");
        }

        otpCode.markUsed();
        otpCodeRepositoryPort.save(otpCode);

        Set<String> roleNames = user.roles().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                user.email(), roleNames, Set.of());

        String accessToken = tokenProvider.generate(authenticatedUser);

        // Use SHA-256 hash for deterministic lookup — BCrypt is non-deterministic
        String rawRefreshToken = UUID.randomUUID().toString();
        String hashedRefreshToken = TokenHasher.hash(rawRefreshToken);
        RefreshToken refreshToken = RefreshToken.issue(user.id(), hashedRefreshToken, clock);
        refreshTokenRepositoryPort.save(refreshToken);

        log.info("Login step2 successful for userId={}", user.id());

        return new LoginStep2Result(
                accessToken,
                "Bearer",
                tokenProvider.expiresInSeconds(),
                user.email(),
                roleNames,
                rawRefreshToken);
    }
}