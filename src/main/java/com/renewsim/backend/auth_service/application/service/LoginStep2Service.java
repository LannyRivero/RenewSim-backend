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
import com.renewsim.backend.auth_service.infrastructure.security.OtpRateLimiter;
import com.renewsim.backend.user_service.application.port.out.UserRepositoryPort;
import com.renewsim.backend.user_service.domain.model.User;
import com.renewsim.backend.shared.exception.AuthenticationException;
import com.renewsim.backend.shared.exception.RateLimitExceededException;
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
 * BUSINESS RULE: Email verification is required before login.
 * Users with unverified emails are blocked at this stage.
 *
 * @since 1.0.0
 */
public class LoginStep2Service implements LoginStep2UseCase {

    private static final Logger log = LoggerFactory.getLogger(LoginStep2Service.class);
    private static final int MAX_OTP_ATTEMPTS = 3;
    private static final int OTP_LOCKOUT_WINDOW_SECONDS = 900; // 15 minutes

    private final UserAccountGateway userAccountGateway;
    private final UserRepositoryPort userRepositoryPort;
    private final OtpCodeRepositoryPort otpCodeRepositoryPort;
    private final RefreshTokenRepositoryPort refreshTokenRepositoryPort;
    private final TokenProvider tokenProvider;
    private final PasswordEncoderPort passwordEncoderPort;
    private final TransactionalPort transactionalPort;
    private final Clock clock;
    private final UserAccountValidator userAccountValidator;
    private final OtpRateLimiter otpRateLimiter;

    public LoginStep2Service(
            UserAccountGateway userAccountGateway,
            UserRepositoryPort userRepositoryPort,
            OtpCodeRepositoryPort otpCodeRepositoryPort,
            RefreshTokenRepositoryPort refreshTokenRepositoryPort,
            TokenProvider tokenProvider,
            PasswordEncoderPort passwordEncoderPort,
            TransactionalPort transactionalPort,
            Clock clock,
            UserAccountValidator userAccountValidator,
            OtpRateLimiter otpRateLimiter) {
        this.userAccountGateway = userAccountGateway;
        this.userRepositoryPort = userRepositoryPort;
        this.otpCodeRepositoryPort = otpCodeRepositoryPort;
        this.refreshTokenRepositoryPort = refreshTokenRepositoryPort;
        this.tokenProvider = tokenProvider;
        this.passwordEncoderPort = passwordEncoderPort;
        this.transactionalPort = transactionalPort;
        this.clock = clock;
        this.userAccountValidator = userAccountValidator;
        this.otpRateLimiter = otpRateLimiter;
    }

    @Override
    public LoginStep2Result execute(LoginStep2Command command) {
        return transactionalPort.execute(() -> executeInternal(command));
    }

    private LoginStep2Result executeInternal(LoginStep2Command command) {
        if (!otpRateLimiter.tryAcquire(command.email())) {
            int retryAfter = otpRateLimiter.secondsUntilReset(command.email());
            log.warn("OTP rate limit exceeded for email={}", command.email());
            throw new RateLimitExceededException(
                    "Too many OTP attempts. Please try again later.",
                    retryAfter);
        }

        UserSnapshot user = userAccountGateway.findByEmail(command.email())
                .orElseThrow(() -> new AuthenticationException("Invalid or expired OTP"));

        userAccountValidator.validateEnabledOrThrow(user);

        // CRITICAL: Validate email is verified before allowing login
        User domainUser = userRepositoryPort.findById(user.id())
                .orElseThrow(() -> new AuthenticationException("User not found"));

        if (!domainUser.canLogin()) {
            log.warn("Login blocked for unverified email userId={}", user.id());
            throw new EmailNotVerifiedException(
                    "Email not verified. Please check your inbox for the verification link.");
        }

        OtpCode otpCode = otpCodeRepositoryPort
                .findLatestValidByUserId(user.id(), OtpCode.Purpose.LOGIN)
                .orElseThrow(() -> new AuthenticationException("Invalid or expired OTP"));

        if (!otpCode.isValid(clock)) {
            throw new AuthenticationException("Invalid or expired OTP");
        }

        if (!passwordEncoderPort.matches(command.otpCode(), otpCode.getCodeHash())) {
            log.debug("Invalid OTP for userId={}", user.id());
            throw new AuthenticationException("Invalid or expired OTP");
        }

        OtpCode usedOtpCode = otpCode.markUsed();
        otpCodeRepositoryPort.save(usedOtpCode);

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

    /**
     * Exception thrown when user attempts to log in without verifying email.
     */
    public static class EmailNotVerifiedException extends AuthenticationException {
        public EmailNotVerifiedException(String message) {
            super(message);
        }
    }
}