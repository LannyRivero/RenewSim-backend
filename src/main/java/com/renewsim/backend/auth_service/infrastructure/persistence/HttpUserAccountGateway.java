package com.renewsim.backend.auth_service.infrastructure.persistence;

import com.renewsim.backend.auth_service.application.dto.UserSnapshot;
import com.renewsim.backend.auth_service.application.port.out.UserAccountGateway;
import com.renewsim.backend.auth_service.infrastructure.client.ExternalUserSnapshot;
import com.renewsim.backend.auth_service.infrastructure.client.UserServiceClient;
import com.renewsim.backend.shared.domain.vo.RoleName;
import com.renewsim.backend.shared.dto.OperationResponse;
import com.renewsim.backend.user_service.domain.model.UserStatus;
import com.renewsim.backend.user_service.web.dto.UserCreateRequest;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
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
            OperationResponse<ExternalUserSnapshot> response = userServiceClient.getCredentials(null, email);
            return Optional.ofNullable(mapToSnapshot(
                    response != null ? response.data() : null));
        } catch (FeignException.NotFound e) {
            return Optional.empty();
        } catch (FeignException e) {
            log.error("Error fetching user by email={}", email, e);
            throw e;
        }
    }

    @Override
    public Optional<UserSnapshot> findByUsername(String username) {
        try {
            OperationResponse<ExternalUserSnapshot> response = userServiceClient.getCredentials(username, null);
            return Optional.ofNullable(mapToSnapshot(
                    response != null ? response.data() : null));
        } catch (FeignException.NotFound e) {
            return Optional.empty();
        } catch (FeignException e) {
            log.error("Error fetching user by username={}", username, e);
            throw e;
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        OperationResponse<Boolean> response = userServiceClient.existsByUsernameOrEmail(username, null);
        return response != null && Boolean.TRUE.equals(response.data());
    }

    @Override
    public boolean existsByEmail(String email) {
        OperationResponse<Boolean> response = userServiceClient.existsByUsernameOrEmail(null, email);
        return response != null && Boolean.TRUE.equals(response.data());
    }

    @Override
    public UserSnapshot createUser(String username, String fullName, String rawPassword,
            String email, Set<RoleName> roles) {
        UserCreateRequest request = new UserCreateRequest(username, email, rawPassword, fullName, null);
        OperationResponse<ExternalUserSnapshot> response = userServiceClient.createUser(request);
        return mapToSnapshot(response != null ? response.data() : null);
    }

    @Override
    public void activateUser(Long userId) {
        try {
            userServiceClient.activateUser(userId);
        } catch (FeignException e) {
            log.error("Error activating userId={}", userId, e);
            throw e;
        }
    }

    @Override
    public Optional<UserSnapshot> findById(Long userId) {
        try {
            OperationResponse<ExternalUserSnapshot> response = userServiceClient.getSnapshotById(userId);
            return Optional.ofNullable(mapToSnapshot(
                    response != null ? response.data() : null));
        } catch (FeignException.NotFound e) {
            return Optional.empty();
        } catch (FeignException e) {
            log.error("Error fetching user by id={}", userId, e);
            throw e;
        }
    }

    private UserSnapshot mapToSnapshot(ExternalUserSnapshot external) {
        if (external == null)
            return null;

        Set<RoleName> roles = Optional.ofNullable(external.roles())
                .orElse(Collections.emptySet())
                .stream()
                .map(RoleName::valueOf)
                .collect(Collectors.toUnmodifiableSet());

        UserStatus status = parseStatus(external.status());
        boolean enabled = status == UserStatus.ACTIVE;

        return new UserSnapshot(
                external.id(),
                external.username(),
                external.fullName(),
                external.passwordHash(),
                external.email(),
                roles,
                status,
                enabled);
    }

    private UserStatus parseStatus(String status) {
        if (status == null)
            return UserStatus.INACTIVE;
        try {
            return UserStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            log.warn("Unknown UserStatus value: {}", status);
            return UserStatus.INACTIVE;
        }
    }
}
