package com.renewsim.backend.auth_service.application.service;

import com.renewsim.backend.auth_service.application.command.RefreshTokenCommand;
import com.renewsim.backend.auth_service.application.dto.UserSnapshot;
import com.renewsim.backend.auth_service.application.port.in.RefreshTokenUseCase;
import com.renewsim.backend.auth_service.application.port.out.RefreshTokenRepositoryPort;
import com.renewsim.backend.auth_service.application.port.out.TokenProvider;
import com.renewsim.backend.auth_service.application.port.out.TransactionalPort;
import com.renewsim.backend.auth_service.application.port.out.UserAccountGateway;
import com.renewsim.backend.auth_service.application.result.RefreshTokenResult;
import com.renewsim.backend.auth_service.application.validator.UserAccountValidator;
import com.renewsim.backend.auth_service.domain.AuthenticatedUser;
import com.renewsim.backend.auth_service.domain.model.RefreshToken;
import com.renewsim.backend.auth_service.domain.service.TokenHasher;
import com.renewsim.backend.shared.exception.AuthenticationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class RefreshTokenService implements RefreshTokenUseCase {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);

    private final RefreshTokenRepositoryPort refreshTokenRepositoryPort;
    private final UserAccountGateway userAccountGateway;
    private final TokenProvider tokenProvider;
    private final TransactionalPort transactionalPort;
    private final Clock clock;
    private final UserAccountValidator userAccountValidator;

    public RefreshTokenService(
            RefreshTokenRepositoryPort refreshTokenRepositoryPort,
            UserAccountGateway userAccountGateway,
            TokenProvider tokenProvider,
            TransactionalPort transactionalPort,
            Clock clock,
            UserAccountValidator userAccountValidator) {
        this.refreshTokenRepositoryPort = refreshTokenRepositoryPort;
        this.userAccountGateway = userAccountGateway;
        this.tokenProvider = tokenProvider;
        this.transactionalPort = transactionalPort;
        this.clock = clock;
        this.userAccountValidator = userAccountValidator;
    }

    @Override
    public RefreshTokenResult execute(RefreshTokenCommand command) {
        return transactionalPort.execute(() -> executeInternal(command));
    }

    private RefreshTokenResult executeInternal(RefreshTokenCommand command) {

        String tokenHash = TokenHasher.hash(command.refreshToken());

        RefreshToken existing = refreshTokenRepositoryPort
                .findByTokenHash(tokenHash)
                .orElseThrow(() -> new AuthenticationException("Invalid or expired refresh token"));

        if (!existing.isValid(clock)) {  // <-- CAMBIO AQUÍ
            throw new AuthenticationException("Invalid or expired refresh token");
        }

        RefreshToken revoked = existing.revoked(clock);
        refreshTokenRepositoryPort.save(revoked);

        UserSnapshot user = userAccountGateway.findById(existing.getUserId())
                .orElseThrow(() -> new AuthenticationException("User not found"));

        userAccountValidator.validateEnabledOrThrow(user);

        Set<String> roleNames = user.roles().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                user.email(), roleNames, Set.of());

        String newAccessToken = tokenProvider.generate(authenticatedUser);

        String newRawRefreshToken = UUID.randomUUID().toString();
        String newHashedRefreshToken = TokenHasher.hash(newRawRefreshToken);
        RefreshToken newRefreshToken = RefreshToken.issue(existing.getUserId(), newHashedRefreshToken, clock);
        refreshTokenRepositoryPort.save(newRefreshToken);

        log.info("AUDIT: Refresh token rotated for userId={}, oldTokenId={}, newTokenId={}, ipAddress={}", 
                existing.getUserId(), existing.getId(), newRefreshToken.getId(), "N/A");

        return new RefreshTokenResult(
                newAccessToken,
                "Bearer",
                tokenProvider.expiresInSeconds(),
                user.email(),
                roleNames,
                newRawRefreshToken);
    }
}