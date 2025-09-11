package com.renewsim.backend.auth_service.infrastructure.persistence;

import com.renewsim.backend.auth_service.application.port.out.UserAccountGateway;
import com.renewsim.backend.auth_service.infrastructure.client.UserServiceClient;
import com.renewsim.backend.auth_service.web.dto.ExternalUserSnapshot;
import com.renewsim.backend.auth_service.web.dto.UserSnapshot;
import com.renewsim.backend.role.RoleName;
import feign.FeignException;
import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class HttpUserAccountGateway implements UserAccountGateway {

    private final UserServiceClient userServiceClient;

    @Override
    public Optional<UserSnapshot> findByUsername(String username) {
        try {
            ExternalUserSnapshot external = userServiceClient.findByUsername(username);
            return Optional.ofNullable(mapToSnapshot(external));
        } catch (FeignException.NotFound e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<UserSnapshot> findByEmail(String email) {
        try {
            ExternalUserSnapshot external = userServiceClient.findByEmail(email);
            return Optional.ofNullable(mapToSnapshot(external));
        } catch (FeignException.NotFound e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        return userServiceClient.existsByUsernameOrEmail(username, null);
    }

    @Override
    public UserSnapshot createUser(String username, String passwordHash, String email, Set<RoleName> roles) {

        ExternalUserSnapshot request = new ExternalUserSnapshot(
                username,
                passwordHash,
                email,
                roles.stream().map(Enum::name).collect(Collectors.toUnmodifiableSet()) // Enum → String
        );

        ExternalUserSnapshot created = userServiceClient.createUser(request);
        return mapToSnapshot(created);
    }

    //  Helper para evitar duplicación de código
    private UserSnapshot mapToSnapshot(ExternalUserSnapshot external) {
        if (external == null) return null;

        Set<RoleName> roles = external.roles().stream()
                .map(RoleName::valueOf)
                .collect(Collectors.toUnmodifiableSet());

        return new UserSnapshot(
                external.username(),
                external.passwordHash(),
                external.email(),
                roles,
                true
        );
    }
}

