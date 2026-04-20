package com.renewsim.backend.auth_service.application.service;

import com.renewsim.backend.auth_service.application.command.LogoutCommand;
import com.renewsim.backend.auth_service.application.port.in.LogoutUseCase;
import com.renewsim.backend.auth_service.application.port.out.RefreshTokenRepositoryPort;
import com.renewsim.backend.auth_service.application.port.out.TokenBlacklistPort;
import com.renewsim.backend.auth_service.application.port.out.TokenProvider;
import com.renewsim.backend.auth_service.application.port.out.UserAccountGateway;
import com.renewsim.backend.auth_service.application.result.LogoutResultDTO;
import com.renewsim.backend.auth_service.web.dto.UserSnapshot;
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
    public LogoutResultDTO execute(LogoutCommand command) {

        tokenProvider.extractJti(command.accessToken()).ifPresent(jti -> {
            long expiresAt = tokenProvider
                    .extractExpirationEpochSeconds(command.accessToken())
                    .orElse(0L);

            Long userId = userAccountGateway.findByEmail(command.username())
                    .map(UserSnapshot::id)
                    .orElse(null);

            if (userId != null) {
                tokenBlacklistPort.blacklist(jti, userId, expiresAt);
                log.info("Token blacklisted for username={} jti={}", command.username(), jti);
            } else {
                log.warn("Could not resolve userId for username={} during logout — token not blacklisted",
                        command.username());
            }
        });

        userAccountGateway.findByEmail(command.username())
                .map(UserSnapshot::id)
                .ifPresent(userId -> {
                    refreshTokenRepositoryPort.revokeAllByUserId(userId);
                    log.info("Refresh tokens revoked for userId={}", userId);
                });

        return new LogoutResultDTO("Logged out successfully");
    }
}