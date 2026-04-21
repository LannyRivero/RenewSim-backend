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
}