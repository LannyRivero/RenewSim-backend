package com.renewsim.backend.auth_service.application.mapper;

import com.renewsim.backend.auth_service.application.dto.UserSnapshot;
import com.renewsim.backend.auth_service.application.port.out.ScopePolicy;
import com.renewsim.backend.auth_service.application.port.out.TokenProvider;
import com.renewsim.backend.auth_service.application.service.TokenTimeService;
import com.renewsim.backend.auth_service.domain.AuthenticatedUser;
import com.renewsim.backend.auth_service.web.dto.AuthResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AuthResponseMapper {
    private final TokenProvider tokenProvider;
    private final ScopePolicy scopePolicy;
    private final TokenTimeService tokenTimeService;

    public AuthResponseDTO toAuthResponseDTO(UserSnapshot user) {
        Instant expireAt = tokenTimeService.calculateExpiration();

        Set<String> roleNames = user.roles().stream()
                .map(Enum::name)
                .collect(Collectors.toUnmodifiableSet());

        Set<String> scopes = scopePolicy.getScopes(user.roles());

        AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                user.username(),
                roleNames,
                scopes);

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
