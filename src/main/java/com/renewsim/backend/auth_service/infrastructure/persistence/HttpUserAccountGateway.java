package com.renewsim.backend.auth_service.infrastructure.persistence;

import com.renewsim.backend.auth_service.application.port.out.UserAccountGateway;
import com.renewsim.backend.auth_service.infrastructure.client.UserServiceClient;
import com.renewsim.backend.auth_service.web.dto.ExternalUserSnapshot;
import com.renewsim.backend.auth_service.web.dto.UserSnapshot;
import com.renewsim.backend.role.RoleName;
import lombok.RequiredArgsConstructor;
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
        ExternalUserSnapshot external = userServiceClient.findByUsername(username);

        if (external == null) {
            return Optional.empty();
        }

        Set<RoleName> roles = external.roles().stream()
                .map(RoleName::valueOf)
                .collect(Collectors.toSet());

        UserSnapshot internal = new UserSnapshot(
                external.username(),
                external.passwordHash(),
                roles);

        return Optional.of(internal);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userServiceClient.existsByUsername(username);
    }

    @Override
    public void createUser(String username, String passwordHash, Set<RoleName> roles) {
        ExternalUserSnapshot external = new ExternalUserSnapshot(
                username,
                passwordHash,
                roles.stream().map(Enum::name).collect(Collectors.toSet()) // Enum → String
        );
        userServiceClient.createUser(external);
    }
}
