package com.renewsim.backend.auth_service.application.port.out;

import com.renewsim.backend.auth_service.domain.model.ActivationToken;

import java.util.Optional;

/**
 * Output port for activation token persistence.
 * No framework dependencies — pure domain contract.
 */
public interface ActivationTokenRepositoryPort {

    ActivationToken save(ActivationToken token);

    Optional<ActivationToken> findByTokenHash(String tokenHash);

    /**
     * Deletes all activation tokens for the given user.
     * Called before issuing a new token to prevent duplicate tokens
     * when a user re-registers or requests a new activation email.
     *
     * @param userId the user whose pending tokens must be removed
     */
    void deleteByUserId(Long userId);
}