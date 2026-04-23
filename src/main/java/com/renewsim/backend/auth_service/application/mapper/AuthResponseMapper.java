package com.renewsim.backend.auth_service.application.mapper;

import com.renewsim.backend.auth_service.application.dto.UserSnapshot;
import com.renewsim.backend.auth_service.application.port.out.ScopePolicy;
import com.renewsim.backend.auth_service.application.port.out.TokenProvider;
import com.renewsim.backend.auth_service.application.result.AuthResult;
import com.renewsim.backend.auth_service.application.service.TokenTimeService;
import com.renewsim.backend.auth_service.domain.AuthenticatedUser;
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

        public AuthResult toAuthResult(UserSnapshot user) {
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

                return new AuthResult(
                                token,
                                "Bearer",
                                expireAt,
                                user.username(),
                                roleNames,
                                scopes);
        }
}