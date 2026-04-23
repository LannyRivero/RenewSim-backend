package com.renewsim.backend.auth_service.application.service;

import com.renewsim.backend.auth_service.application.command.LogoutCommand;
import com.renewsim.backend.auth_service.application.dto.UserSnapshot;
import com.renewsim.backend.auth_service.application.port.in.LogoutUseCase;
import com.renewsim.backend.auth_service.application.port.out.RefreshTokenRepositoryPort;
import com.renewsim.backend.auth_service.application.port.out.TokenBlacklistPort;
import com.renewsim.backend.auth_service.application.port.out.TokenProvider;
import com.renewsim.backend.auth_service.application.port.out.UserAccountGateway;
import com.renewsim.backend.auth_service.application.result.LogoutResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutUseCase {

    private final TokenProvider tokenProvider;
    private final TokenBlacklistPort tokenBlacklistPort;
    private final RefreshTokenRepositoryPort refreshTokenRepositoryPort;
    private final UserAccountGateway userAccountGateway;

    @Override
    @Transactional
    public LogoutResult execute(LogoutCommand command) {

        Long userId = userAccountGateway.findByEmail(command.username())
                .map(UserSnapshot::id)
                .orElse(null);

        // Blacklist JTI siempre que exista — aunque userId sea null.
        // Un token debe invalidarse independientemente de si el usuario se resuelve.
        tokenProvider.extractJti(command.accessToken()).ifPresent(jti -> {
            long expiresAt = tokenProvider
                    .extractExpirationEpochSeconds(command.accessToken())
                    .orElse(0L);

            tokenBlacklistPort.blacklist(jti, userId, expiresAt);
            log.info("Token blacklisted jti={} userId={}", jti, userId);
        });

        // Revocar refresh tokens solo si el usuario existe
        if (userId != null) {
            refreshTokenRepositoryPort.revokeAllByUserId(userId);
            log.info("Refresh tokens revoked for userId={}", userId);
        } else {
            log.warn("Could not resolve userId for username={} during logout — refresh tokens not revoked",
                    command.username());
        }

        return new LogoutResult("Logged out successfully");
    }
}