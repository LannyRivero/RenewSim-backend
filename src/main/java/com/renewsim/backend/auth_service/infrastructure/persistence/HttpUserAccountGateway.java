package com.renewsim.backend.auth_service.infrastructure.persistence;

import com.renewsim.backend.auth_service.application.port.out.UserAccountGateway;
import com.renewsim.backend.auth_service.infrastructure.client.UserServiceClient;
import com.renewsim.backend.auth_service.web.dto.ExternalUserSnapshot;
import com.renewsim.backend.auth_service.web.dto.UserSnapshot;
import com.renewsim.backend.role_service.domain.model.RoleName;
import com.renewsim.backend.user_service.dto.UserCreateRequest;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class HttpUserAccountGateway implements UserAccountGateway {

    private final UserServiceClient userServiceClient;

    @Override
    public Optional<UserSnapshot> findByEmail(String email) {
        try {
            ExternalUserSnapshot external = userServiceClient.getCredentials(null, email);

            log.debug("Fetched ExternalUserSnapshot: username={}, email={}",
                    external.username(),
                    external.email());

            return Optional.ofNullable(mapToSnapshot(external));
        } catch (FeignException.NotFound e) {
            return Optional.empty();
        } catch (FeignException e) {
            log.error("Error fetching user by email={} from UserServiceClient", email, e);
            throw e;
        }
    }

    @Override
    public Optional<UserSnapshot> findByUsername(String username) {
        try {
            ExternalUserSnapshot external = userServiceClient.getCredentials(username, null);

            log.debug("Fetched ExternalUserSnapshot: username={}, email={}",
                    external.username(),
                    external.email());

            return Optional.ofNullable(mapToSnapshot(external));
        } catch (FeignException.NotFound e) {
            return Optional.empty();
        } catch (FeignException e) {
            log.error("Error fetching user by username={} from UserServiceClient", username, e);
            throw e;
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        return userServiceClient.existsByUsernameOrEmail(username, null);
    }

    @Override
    public UserSnapshot createUser(String username, String rawPassword, String email, Set<RoleName> roles) {

        UserCreateRequest request = new UserCreateRequest(username, email, rawPassword);

        ExternalUserSnapshot created = userServiceClient.createUser(request);
        return mapToSnapshot(created);
    }

    // Helper para evitar duplicación de código
    private UserSnapshot mapToSnapshot(ExternalUserSnapshot external) {
        if (external == null)
            return null;

        Set<RoleName> roles = external.roles().stream()
                .map(RoleName::valueOf)
                .collect(Collectors.toUnmodifiableSet());

        return new UserSnapshot(
                external.username(),
                external.passwordHash(),
                external.email(),
                roles,
                true);
    }
}
