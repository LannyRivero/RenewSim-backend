package com.renewsim.backend.auth_service.application.service;

import com.renewsim.backend.auth_service.application.port.in.AuthUseCase;
import com.renewsim.backend.auth_service.application.port.out.ActivationTokenRepositoryPort;
import com.renewsim.backend.auth_service.application.port.out.EmailPort;
import com.renewsim.backend.auth_service.application.port.out.UserAccountGateway;
import com.renewsim.backend.auth_service.application.mapper.AuthResponseMapper;
import com.renewsim.backend.auth_service.domain.AuthValidator;
import com.renewsim.backend.auth_service.domain.model.ActivationToken;
import com.renewsim.backend.auth_service.domain.service.TokenHasher;
import com.renewsim.backend.auth_service.web.dto.AuthRequestDTO;
import com.renewsim.backend.auth_service.web.dto.AuthResponseDTO;
import com.renewsim.backend.auth_service.web.dto.RegisterRequestDTO;
import com.renewsim.backend.auth_service.web.dto.RegisterResponseDTO;
import com.renewsim.backend.auth_service.web.dto.UserSnapshot;
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
    private final AuthValidator authValidator;
    private final AuthResponseMapper authResponseMapper;
    private final ActivationTokenRepositoryPort activationTokenRepositoryPort;
    private final EmailPort emailPort;

    @Override
    public AuthResponseDTO login(AuthRequestDTO request) {
        authValidator.validateCredentials(request);

        String loginInput = request.getUsername();

        UserSnapshot user = (loginInput.contains("@")
                ? userAccountGateway.findByEmail(loginInput)
                : userAccountGateway.findByUsername(loginInput))
                .orElseThrow(() -> new AuthenticationException(
                        ErrorMessageFactory.build(AUTH_INVALID_CREDENTIALS)));

        authValidator.validateUserEnable(user.enabled());
        authValidator.validatePassword(request.getPassword(), user.passwordHash());

        return authResponseMapper.toAuthResponseDTO(user);
    }

    @Override
    @Transactional
    public RegisterResponseDTO register(RegisterRequestDTO request) {
        // Structural validation is covered by @Valid in the controller.
        // Here we only enforce business invariants.
        if (userAccountGateway.existsByEmail(request.email())) {
            throw new ResourceConflictException(
                    AUTH_EMAIL_CONFLICT.code(),
                    AUTH_EMAIL_CONFLICT.defaultMessage());
        }

        UserSnapshot user = userAccountGateway.createUser(
                request.fullName(),
                request.password(),
                request.email(),
                Set.of(RoleName.USER));

        // Invalidate any previous activation tokens for this user (idempotent
        // re-register guard)
        activationTokenRepositoryPort.deleteByUserId(user.id());

        // Generate a cryptographically random token (UUID v4 = 122 bits entropy).
        // Only the SHA-256 hash is stored in DB; the raw value is sent to the user.
        String rawToken = UUID.randomUUID().toString();
        String tokenHash = TokenHasher.hash(rawToken);

        ActivationToken activationToken = ActivationToken.issue(user.id(), tokenHash);
        activationTokenRepositoryPort.save(activationToken);

        // Deliver activation email — adapter is profile-specific
        emailPort.sendActivationEmail(user.email(), rawToken);
        log.info("Activation email sent for userId={}", user.id());

        return new RegisterResponseDTO(
                user.id(),
                user.email(),
                user.fullName(),
                user.status(),
                "User registered successfully. Please check your email to activate your account.");
    }
}