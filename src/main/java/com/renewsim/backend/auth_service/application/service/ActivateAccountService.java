package com.renewsim.backend.auth_service.application.service;

import com.renewsim.backend.auth_service.application.command.ActivateAccountCommand;
import com.renewsim.backend.auth_service.application.port.in.ActivateAccountUseCase;
import com.renewsim.backend.auth_service.application.port.out.ActivationTokenRepositoryPort;
import com.renewsim.backend.auth_service.application.port.out.TransactionalPort;
import com.renewsim.backend.auth_service.application.port.out.UserAccountGateway;
import com.renewsim.backend.auth_service.application.result.ActivateAccountResult;
import com.renewsim.backend.auth_service.domain.model.ActivationToken;
import com.renewsim.backend.auth_service.domain.service.TokenHasher;
import com.renewsim.backend.shared.exception.AuthenticationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;

/**
 * Implementación del caso de uso de activación de cuenta.
 *
 * Valida el token de activación y activa la cuenta del usuario
 * en el sistema.
 *
 * @since 1.0.0
 */
public class ActivateAccountService implements ActivateAccountUseCase {

    private static final Logger log = LoggerFactory.getLogger(ActivateAccountService.class);

    private final ActivationTokenRepositoryPort activationTokenRepositoryPort;
    private final UserAccountGateway userAccountGateway;
    private final TransactionalPort transactionalPort;
    private final Clock clock;

    public ActivateAccountService(
            ActivationTokenRepositoryPort activationTokenRepositoryPort,
            UserAccountGateway userAccountGateway,
            TransactionalPort transactionalPort,
            Clock clock) {
        this.activationTokenRepositoryPort = activationTokenRepositoryPort;
        this.userAccountGateway = userAccountGateway;
        this.transactionalPort = transactionalPort;
        this.clock = clock;
    }

    @Override
    public ActivateAccountResult execute(ActivateAccountCommand command) {
        return transactionalPort.execute(() -> executeInternal(command));
    }

    private ActivateAccountResult executeInternal(ActivateAccountCommand command) {
        String rawToken = command.token();
        String tokenHash = TokenHasher.hash(rawToken);

        ActivationToken token = activationTokenRepositoryPort
                .findByTokenHash(tokenHash)
                .orElseThrow(() -> new AuthenticationException("Invalid or expired activation token"));

        if (!token.isValid(clock)) {  // <-- CAMBIO AQUÍ
            throw new AuthenticationException("Invalid or expired activation token");
        }

        userAccountGateway.activateUser(token.getUserId());

        token.markUsed();
        activationTokenRepositoryPort.save(token);

        log.info("Account activated for userId={}", token.getUserId());

        return new ActivateAccountResult("Account activated successfully");
    }
}