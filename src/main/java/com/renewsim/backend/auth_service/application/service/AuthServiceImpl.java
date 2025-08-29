package com.renewsim.backend.auth_service.application.service;

import com.renewsim.backend.auth_service.application.port.in.AuthUseCase;
import com.renewsim.backend.auth_service.application.port.out.RoleProvider;
import com.renewsim.backend.auth_service.application.port.out.ScopePolicy;
import com.renewsim.backend.auth_service.application.port.out.TokenProvider;
import com.renewsim.backend.auth_service.application.port.out.UserAccountGateway;
import com.renewsim.backend.auth_service.domain.AuthenticatedUser;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthUseCase {

    private static final String INVALID_MSG = "Invalid username or password";
    private static final String MDC_KEY = "correlationId";

    private final UserAccountGateway userGateway;
    private final RoleProvider roleProvider;
    private final ScopePolicy scopePolicy;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final Clock clock;

    @Override
    public AuthResponseDTO login(AuthRequestDTO request) {
        final String normUsername = normalize(request.getUsername());

        UserSnapshot user = userGateway.findByUsername(normUsername)
                .orElseThrow(() -> {
                    log.warn("Login failed: bad credentials user={} corr={}", normUsername, MDC.get(MDC_KEY));
                    return new AuthenticationException(INVALID_MSG);
                });

        if (!passwordEncoder.matches(request.getPassword(), user.passwordHash())) {
            log.warn("Login failed: bad credentials user={} corr={}", normUsername, MDC.get(MDC_KEY));
            throw new AuthenticationException(INVALID_MSG);
        }

        Set<String> roleNames = user.roles().stream()
                .map(Enum::name)
                .collect(Collectors.toUnmodifiableSet());

        Set<String> scopes = user.roles().stream()
                .flatMap(r -> scopePolicy.scopesFor(r).stream())
                .collect(Collectors.toUnmodifiableSet());

        String token = tokenProvider.generate(new AuthenticatedUser(user.username(), roleNames, scopes));
        log.info("Login success user={} corr={}", normUsername, MDC.get(MDC_KEY));

        return buildResponse(token, user.username(), roleNames, scopes);
    }

    @Override
    @Transactional
    public AuthResponseDTO register(AuthRequestDTO request) {
        final String normUsername = normalize(request.getUsername());

        if (userGateway.existsByUsername(normUsername)) {
            log.warn("Register failed: username exists user={} corr={}", normUsername, MDC.get(MDC_KEY));
            throw new ResourceConflictException("Username already exists");
        }

        RoleName defaultRole = roleProvider.defaultRole();
        Set<RoleName> roles = Set.of(defaultRole);

        String hash = passwordEncoder.encode(request.getPassword());

        try {
            userGateway.createUser(normUsername, hash, roles);
        } catch (DataIntegrityViolationException ex) {
            log.warn("Register conflict (race) user={} corr={} reason={}", normUsername, MDC.get(MDC_KEY),
                    ex.getMessage());
            throw new ResourceConflictException("Username already exists");
        }

        Set<String> roleNames = roles.stream()
                .map(Enum::name)
                .collect(Collectors.toUnmodifiableSet());

        Set<String> scopes = scopePolicy.scopesFor(defaultRole)
                .stream()
                .collect(Collectors.toUnmodifiableSet());

        String token = tokenProvider.generate(new AuthenticatedUser(normUsername, roleNames, scopes));
        log.info("Register success user={} role={} corr={}", normUsername, defaultRole.name(), MDC.get(MDC_KEY));

        return buildResponse(token, normUsername, roleNames, scopes);
    }

    private AuthResponseDTO buildResponse(String token, String username, Set<String> roles, Set<String> scopes) {
        Instant now = Instant.now(clock);
        long expiresIn = tokenProvider.expiresInSeconds();

        Set<String> rolesCopy = Set.copyOf(roles);
        Set<String> scopesCopy = Set.copyOf(scopes);

        return AuthResponseDTO.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresAt(now.plusSeconds(expiresIn))
                .username(username)
                .roles(rolesCopy)
                .scopes(scopesCopy)
                .build();
    }

    private String normalize(String username) {
        if (username == null)
            return null;
        return username.trim().toLowerCase(Locale.ROOT);
    }
}
