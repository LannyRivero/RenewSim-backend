package com.renewsim.backend.auth_service.application.service;

import com.renewsim.backend.auth_service.application.command.LoginCommand;
import com.renewsim.backend.auth_service.application.dto.UserSnapshot;
import com.renewsim.backend.auth_service.application.port.in.LoginUseCase;
import com.renewsim.backend.auth_service.application.port.out.RefreshTokenRepositoryPort;
import com.renewsim.backend.auth_service.application.port.out.TokenProvider;
import com.renewsim.backend.auth_service.application.port.out.TransactionalPort;
import com.renewsim.backend.auth_service.application.port.out.UserAccountGateway;
import com.renewsim.backend.auth_service.application.result.LoginResult;
import com.renewsim.backend.auth_service.application.validator.CredentialsValidator;
import com.renewsim.backend.auth_service.domain.AuthenticatedUser;
import com.renewsim.backend.auth_service.domain.model.RefreshToken;
import com.renewsim.backend.shared.domain.vo.RoleName;
import com.renewsim.backend.shared.error.ErrorMessageFactory;
import com.renewsim.backend.shared.exception.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.util.Set;
import java.util.stream.Collectors;

import static com.renewsim.backend.auth_service.domain.error.AuthErrorCode.AUTH_INVALID_CREDENTIALS;
import static com.renewsim.backend.auth_service.domain.error.AuthErrorCode.AUTH_USER_DISABLED;

/**
 * Login service - authenticates users via email and password.
 *
 * <p>
 * Flow:
 * 1. Validate credentials format
 * 2. Find user by email
 * 3. Verify password hash
 * 4. Check email is verified (enabled flag)
 * 5. Check account is active
 * 6. Generate JWT access token
 * 7. Generate refresh token with configured TTL
 * 8. Store refresh token
 */
public class LoginService implements LoginUseCase {

        private static final Logger log = LoggerFactory.getLogger(LoginService.class);

        private final UserAccountGateway userAccountGateway;
        private final CredentialsValidator credentialsValidator;
        private final TokenProvider tokenProvider;
        private final RefreshTokenRepositoryPort refreshTokenRepository;
        private final TransactionalPort transactionalPort;
        private final TokenTimeService tokenTimeService;
        private final Clock clock;

        public LoginService(
                        UserAccountGateway userAccountGateway,
                        CredentialsValidator credentialsValidator,
                        TokenProvider tokenProvider,
                        RefreshTokenRepositoryPort refreshTokenRepository,
                        TransactionalPort transactionalPort,
                        TokenTimeService tokenTimeService,
                        Clock clock) {
                this.userAccountGateway = userAccountGateway;
                this.credentialsValidator = credentialsValidator;
                this.tokenProvider = tokenProvider;
                this.refreshTokenRepository = refreshTokenRepository;
                this.transactionalPort = transactionalPort;
                this.tokenTimeService = tokenTimeService;
                this.clock = clock;
        }

        @Override
        public LoginResult execute(LoginCommand command) {
                return transactionalPort.execute(() -> executeInternal(command));
        }

        private LoginResult executeInternal(LoginCommand command) {
                // 1. Validate format
                credentialsValidator.validateCredentials(command.email(), command.password());

                // 2. Find user
                UserSnapshot user = userAccountGateway.findByEmail(command.email())
                                .orElseThrow(() -> {
                                        log.warn("Login failed: user not found email={}", maskEmail(command.email()));
                                        return new UnauthorizedException(
                                                        ErrorMessageFactory.build(AUTH_INVALID_CREDENTIALS));
                                });

                // 3. Verify password
                credentialsValidator.validatePassword(command.password(), user.passwordHash());

                // 4 & 5. Check email verified AND account active (both via enabled flag)
                if (!user.enabled()) {
                        log.warn("Login failed: account disabled or email not verified userId={} email={}",
                                        user.id(), maskEmail(command.email()));
                        throw new UnauthorizedException(
                                        ErrorMessageFactory.build(AUTH_USER_DISABLED));
                }

                // 6. Generate access token
                Set<String> roleNames = user.roles().stream()
                                .map(RoleName::name)
                                .collect(Collectors.toSet());

                AuthenticatedUser authenticatedUser = AuthenticatedUser.of(
                                user.username(),
                                roleNames,
                                Set.of());

                String accessToken = tokenProvider.generate(authenticatedUser);

                // 7. Generate refresh token with configured TTL
                AuthenticatedUser refreshUser = AuthenticatedUser.of(
                                user.username(),
                                roleNames,
                                Set.of("refresh"));
                long refreshTtl = tokenProvider.refreshExpiresInSeconds();
                String refreshToken = tokenProvider.generate(refreshUser, refreshTtl);

                // 8. Store refresh token
                RefreshToken refreshTokenEntity = RefreshToken.issue(
                                user.id(),
                                refreshToken,
                                clock,
                                refreshTtl);
                refreshTokenRepository.save(refreshTokenEntity);

                log.info("User logged in successfully userId={} email={}",
                                user.id(), maskEmail(command.email()));

                return new LoginResult(
                                accessToken,
                                refreshToken,
                                "Bearer",
                                tokenTimeService.getAccessTokenValiditySeconds(),
                                user.id(),
                                user.username(),
                                roleNames);
        }

        private String maskEmail(String email) {
                if (email == null || !email.contains("@"))
                        return "***";
                int at = email.indexOf("@");
                return at <= 2 ? "***" + email.substring(at) : email.substring(0, 2) + "***" + email.substring(at);
        }
}