package com.renewsim.backend.auth_service.application.port.out;

import com.renewsim.backend.auth_service.domain.model.RefreshToken;

import java.util.Optional;

/**
 * Output port for refresh token persistence.
 * No framework dependencies — pure domain contract.
 */
public interface RefreshTokenRepositoryPort {

    RefreshToken save(RefreshToken token);

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    void revokeAllByUserId(Long userId);
}