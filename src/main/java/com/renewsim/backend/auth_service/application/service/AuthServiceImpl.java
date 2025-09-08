package com.renewsim.backend.auth_service.application.service;

import com.renewsim.backend.auth_service.application.port.in.AuthUseCase;
import com.renewsim.backend.auth_service.application.port.out.RoleProvider;
import com.renewsim.backend.auth_service.application.port.out.ScopePolicy;
import com.renewsim.backend.auth_service.application.port.out.TokenProvider;
import com.renewsim.backend.auth_service.application.port.out.UserAccountGateway;
import com.renewsim.backend.auth_service.domain.AuthValidator;
import com.renewsim.backend.auth_service.domain.AuthenticatedUser;
import com.renewsim.backend.auth_service.domain.TokenTimeService;
import com.renewsim.backend.auth_service.web.dto.AuthRequestDTO;
import com.renewsim.backend.auth_service.web.dto.AuthResponseDTO;
import com.renewsim.backend.auth_service.web.dto.UserSnapshot;
import com.renewsim.backend.role.RoleName;
import com.renewsim.backend.shared.exception.AuthenticationException;
import com.renewsim.backend.shared.exception.ResourceConflictException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthUseCase {

    private final UserAccountGateway userAccountGateway;
    private final TokenProvider tokenProvider;
    private final RoleProvider roleProvider;
    private final ScopePolicy scopePolicy;
    private final AuthValidator authValidator;
    private final TokenTimeService tokenTimeService;

    @Override
    public AuthResponseDTO login(AuthRequestDTO request) {

        authValidator.validateCredentials(request);

        UserSnapshot user = userAccountGateway.findByUsername(request.getUsername())
                .orElseThrow(() -> new AuthenticationException("Invalid credentials"));

        authValidator.validateUserEnable(user.enabled());

        Instant expireAt = tokenTimeService.calculateExpiration();

        Set<String> scopes = scopePolicy.getScopes(user.roles());

        Set<String> roleNames = user.roles().stream()
                .map(Enum::name)
                .collect(Collectors.toUnmodifiableSet());

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                user.username(),
                roleNames,
                scopes
                );

        String token = tokenProvider.generate(authenticatedUser);

        return AuthResponseDTO.builder()
            .token(token)
            .tokenType("Bearer")
            .expiresAt(expireAt)
            .username(user.username())
            .roles(roleNames)
            .scopes(scopes)
            .build();

    }

}
