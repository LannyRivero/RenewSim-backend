package com.renewsim.backend.auth_service.application.service;

import com.renewsim.backend.auth_service.application.command.RefreshTokenCommand;
import com.renewsim.backend.auth_service.application.port.in.RefreshTokenUseCase;
import com.renewsim.backend.auth_service.application.port.out.RefreshTokenRepositoryPort;
import com.renewsim.backend.auth_service.application.port.out.TokenProvider;
import com.renewsim.backend.auth_service.application.port.out.UserAccountGateway;
import com.renewsim.backend.auth_service.application.result.RefreshTokenResultDTO;
import com.renewsim.backend.auth_service.domain.AuthenticatedUser;
import com.renewsim.backend.auth_service.domain.model.RefreshToken;
import com.renewsim.backend.auth_service.web.dto.UserSnapshot;
import com.renewsim.backend.shared.exception.AuthenticationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService implements RefreshTokenUseCase {

    private final RefreshTokenRepositoryPort refreshTokenRepositoryPort;
    private final UserAccountGateway userAccountGateway;
    private final TokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public RefreshTokenResultDTO execute(RefreshTokenCommand command) {

        RefreshToken existing = refreshTokenRepositoryPort
                .findByTokenHash(command.refreshToken())
                .orElseThrow(() -> new AuthenticationException("Invalid or expired refresh token"));

        if (!existing.isValid()) {
            throw new AuthenticationException("Invalid or expired refresh token");
        }

        // Revoke the old token
        existing.revoke();
        refreshTokenRepositoryPort.save(existing);

        // Load user by ID
        UserSnapshot user = userAccountGateway.findById(existing.getUserId())
                .orElseThrow(() -> new AuthenticationException("User not found"));

        if (!user.enabled()) {
            throw new AuthenticationException("Account is not active");
        }

        // Generate new access token
        Set<String> roleNames = user.roles().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                user.email(), roleNames, Set.of());

        String newAccessToken = tokenProvider.generate(authenticatedUser);

        // Issue new refresh token
        String rawNewRefreshToken = UUID.randomUUID().toString();
        String hashedNewRefreshToken = passwordEncoder.encode(rawNewRefreshToken);
        RefreshToken newRefreshToken = RefreshToken.issue(existing.getUserId(), hashedNewRefreshToken);
        refreshTokenRepositoryPort.save(newRefreshToken);

        log.info("Refresh token rotated for userId={}", existing.getUserId());

        return new RefreshTokenResultDTO(
                newAccessToken,
                "Bearer",
                tokenProvider.expiresInSeconds(),
                user.email(),
                roleNames);
    }
}