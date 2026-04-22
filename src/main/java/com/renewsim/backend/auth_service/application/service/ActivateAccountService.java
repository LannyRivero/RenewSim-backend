package com.renewsim.backend.auth_service.application.service;

import com.renewsim.backend.auth_service.application.command.ActivateAccountCommand;
import com.renewsim.backend.auth_service.application.port.in.ActivateAccountUseCase;
import com.renewsim.backend.auth_service.application.port.out.ActivationTokenRepositoryPort;
import com.renewsim.backend.auth_service.application.port.out.UserAccountGateway;
import com.renewsim.backend.auth_service.application.result.ActivateAccountResultDTO;
import com.renewsim.backend.auth_service.domain.model.ActivationToken;
import com.renewsim.backend.auth_service.domain.service.TokenHasher;
import com.renewsim.backend.shared.exception.AuthenticationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivateAccountService implements ActivateAccountUseCase {

    private final ActivationTokenRepositoryPort activationTokenRepositoryPort;
    private final UserAccountGateway userAccountGateway;

    @Override
    @Transactional
    public ActivateAccountResultDTO execute(ActivateAccountCommand command) {
        String rawToken = command.token();
        String tokenHash = TokenHasher.hash(rawToken);

        ActivationToken token = activationTokenRepositoryPort
                .findByTokenHash(tokenHash)
                .orElseThrow(() -> new AuthenticationException("Invalid or expired activation token"));

        if (!token.isValid()) {
            throw new AuthenticationException("Invalid or expired activation token");
        }

        userAccountGateway.activateUser(token.getUserId());

        token.markUsed();
        activationTokenRepositoryPort.save(token);

        log.info("Account activated for userId={}", token.getUserId());

        return new ActivateAccountResultDTO("Account activated successfully");
    }
}