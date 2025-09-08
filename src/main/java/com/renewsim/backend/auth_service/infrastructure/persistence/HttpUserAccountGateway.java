package com.renewsim.backend.auth_service.infrastructure.persistence;

import com.renewsim.backend.auth_service.application.port.out.UserAccountGateway;
import com.renewsim.backend.auth_service.infrastructure.client.UserServiceClient;
import com.renewsim.backend.auth_service.web.dto.ExternalUserSnapshot;
import com.renewsim.backend.auth_service.web.dto.UserSnapshot;
import com.renewsim.backend.role.RoleName;
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
    private final PasswordEncoder passwordEncoder;

    @Override
    public Optional<UserSnapshot> findByUsername(String username) {
        ExternalUserSnapshot external = userServiceClient.findByUsername(username);

        if (external == null) {
            return Optional.empty();
        }

        Set<RoleName> roles = external.roles().stream()
                .map(RoleName::valueOf)
                .collect(Collectors.toUnmodifiableSet());

        return Optional.of(new UserSnapshot(
                external.username(),
                external.passwordHash(),
                roles,
                true));
    }

    @Override
    public boolean existsByUsername(String username) {
        return userServiceClient.existsByUsername(username);
    }

    @Override
    public UserSnapshot createUser(String username, String rawPassword, Set<RoleName> roles) {

        String passwordHash = passwordEncoder.encode(rawPassword);

        ExternalUserSnapshot request = new ExternalUserSnapshot(
                username,
                passwordHash,
                roles.stream().map(Enum::name).collect(Collectors.toUnmodifiableSet()) // Enum → String
        );
        ExternalUserSnapshot created = userServiceClient.createUser(request);

        Set<RoleName> mappedRoles = created.roles().stream()
                .map(RoleName::valueOf)
                .collect(Collectors.toUnmodifiableSet());

        return new UserSnapshot(
                created.username(),
                created.passwordHash(),
                mappedRoles,
                true);
    }
}
