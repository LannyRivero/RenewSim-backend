package com.renewsim.backend.auth_service.application.service;

import com.renewsim.backend.auth_service.application.command.LoginStep2Command;
import com.renewsim.backend.auth_service.application.dto.UserSnapshot;
import com.renewsim.backend.auth_service.application.port.in.LoginStep2UseCase;
import com.renewsim.backend.auth_service.application.port.out.OtpCodeRepositoryPort;
import com.renewsim.backend.auth_service.application.port.out.RefreshTokenRepositoryPort;
import com.renewsim.backend.auth_service.application.port.out.TokenProvider;
import com.renewsim.backend.auth_service.application.port.out.UserAccountGateway;
import com.renewsim.backend.auth_service.application.result.LoginStep2ResultDTO;
import com.renewsim.backend.auth_service.application.validator.CredentialsValidator;
import com.renewsim.backend.auth_service.domain.AuthenticatedUser;
import com.renewsim.backend.auth_service.domain.model.OtpCode;
import com.renewsim.backend.auth_service.domain.model.RefreshToken;
import com.renewsim.backend.auth_service.domain.service.TokenHasher;
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
public class LoginStep2Service implements LoginStep2UseCase {

    private final UserAccountGateway userAccountGateway;
    private final OtpCodeRepositoryPort otpCodeRepositoryPort;
    private final RefreshTokenRepositoryPort refreshTokenRepositoryPort;
    private final TokenProvider tokenProvider;
    private final CredentialsValidator credentialsValidator;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public LoginStep2ResultDTO execute(LoginStep2Command command) {

        UserSnapshot user = userAccountGateway.findByEmail(command.email())
                .orElseThrow(() -> new AuthenticationException("Invalid credentials"));

        if (!user.enabled()) {
            throw new AuthenticationException("Account is not active");
        }

        OtpCode otpCode = otpCodeRepositoryPort
                .findLatestValidByUserId(user.id(), OtpCode.Purpose.LOGIN)
                .orElseThrow(() -> new AuthenticationException("Invalid or expired OTP"));

        if (!otpCode.isValid()) {
            throw new AuthenticationException("Invalid or expired OTP");
        }

        if (!passwordEncoder.matches(command.otpCode(), otpCode.getCodeHash())) {
            throw new AuthenticationException("Invalid or expired OTP");
        }

        otpCode.markUsed();
        otpCodeRepositoryPort.save(otpCode);

        Set<String> roleNames = user.roles().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                user.email(), roleNames, Set.of());

        String accessToken = tokenProvider.generate(authenticatedUser);

        // Use SHA-256 hash for deterministic lookup — BCrypt is non-deterministic
        String rawRefreshToken = UUID.randomUUID().toString();
        String hashedRefreshToken = TokenHasher.hash(rawRefreshToken);
        RefreshToken refreshToken = RefreshToken.issue(user.id(), hashedRefreshToken);
        refreshTokenRepositoryPort.save(refreshToken);

        log.info("Login step2 successful for userId={}", user.id());

        return new LoginStep2ResultDTO(
                accessToken,
                "Bearer",
                tokenProvider.expiresInSeconds(),
                user.email(),
                roleNames,
                rawRefreshToken);
    }
}