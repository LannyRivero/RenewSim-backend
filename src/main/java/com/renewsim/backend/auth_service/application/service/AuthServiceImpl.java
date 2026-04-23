package com.renewsim.backend.auth_service.application.service;

import com.renewsim.backend.auth_service.application.command.AuthCommand;
import com.renewsim.backend.auth_service.application.command.RegisterCommand;
import com.renewsim.backend.auth_service.application.dto.UserSnapshot;
import com.renewsim.backend.auth_service.application.port.in.AuthUseCase;
import com.renewsim.backend.auth_service.application.port.out.ActivationTokenRepositoryPort;
import com.renewsim.backend.auth_service.application.port.out.EmailPort;
import com.renewsim.backend.auth_service.application.port.out.UserAccountGateway;
import com.renewsim.backend.auth_service.application.mapper.AuthResponseMapper;
import com.renewsim.backend.auth_service.application.result.AuthResult;
import com.renewsim.backend.auth_service.application.result.RegisterResult;
import com.renewsim.backend.auth_service.application.validator.CredentialsValidator;
import com.renewsim.backend.auth_service.domain.model.ActivationToken;
import com.renewsim.backend.auth_service.domain.service.TokenHasher;
import com.renewsim.backend.shared.domain.vo.RoleName;
import com.renewsim.backend.shared.error.ErrorMessageFactory;
import com.renewsim.backend.shared.exception.AuthenticationException;
import com.renewsim.backend.shared.exception.ResourceConflictException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

import static com.renewsim.backend.auth_service.domain.error.AuthErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthUseCase {

    private final UserAccountGateway userAccountGateway;
    private final CredentialsValidator credentialsValidator;
    private final AuthResponseMapper authResponseMapper;
    private final ActivationTokenRepositoryPort activationTokenRepositoryPort;
    private final EmailPort emailPort;

    @Override
    public AuthResult login(AuthCommand command) {
        credentialsValidator.validateCredentials(command.username(), command.password());

        String loginInput = command.username();

        UserSnapshot user = (loginInput.contains("@")
                ? userAccountGateway.findByEmail(loginInput)
                : userAccountGateway.findByUsername(loginInput))
                .orElseThrow(() -> new AuthenticationException(
                        ErrorMessageFactory.build(AUTH_INVALID_CREDENTIALS)));

        credentialsValidator.validateUserEnabled(user.enabled());
        credentialsValidator.validatePassword(command.password(), user.passwordHash());

        return authResponseMapper.toAuthResult(user);
    }

    @Override
    @Transactional
    public RegisterResult register(RegisterCommand command) {
        if (userAccountGateway.existsByEmail(command.email())) {
            throw new ResourceConflictException(
                    AUTH_EMAIL_CONFLICT.code(),
                    AUTH_EMAIL_CONFLICT.defaultMessage());
        }

        String username = deriveUsername(command.email());

        UserSnapshot user = userAccountGateway.createUser(
                username,
                command.fullName(),
                command.password(),
                command.email(),
                Set.of(RoleName.USER));

        String rawToken = UUID.randomUUID().toString();
        String tokenHash = TokenHasher.hash(rawToken);

        ActivationToken activationToken = ActivationToken.issue(user.id(), tokenHash);
        activationTokenRepositoryPort.save(activationToken);

        emailPort.sendActivationEmail(user.email(), rawToken);
        log.info("Activation email sent for userId={}", user.id());

        return new RegisterResult(
                user.id(),
                user.email(),
                user.fullName(),
                user.status(),
                "User registered successfully. Please check your email to activate your account.");
    }

    // Regla de negocio: derivar username válido (^[a-z0-9._-]{3,32}$) desde email
    private String deriveUsername(String email) {
        String localPart = email.split("@")[0].toLowerCase();
        localPart = localPart.replaceAll("[^a-z0-9._-]", ".");
        return localPart.length() > 32 ? localPart.substring(0, 32) : localPart;
    }
}
