package com.renewsim.backend.auth_service.infrastructure.persistence;

import com.renewsim.backend.auth_service.application.dto.UserSnapshot;
import com.renewsim.backend.auth_service.application.port.out.UserAccountGateway;
import com.renewsim.backend.auth_service.infrastructure.client.ExternalUserSnapshot;
import com.renewsim.backend.auth_service.infrastructure.client.UserServiceClient;
import com.renewsim.backend.auth_service.infrastructure.mapper.UserSnapshotMapper;
import com.renewsim.backend.shared.dto.OperationResponse;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class HttpUserAccountGateway implements UserAccountGateway {

    private final UserServiceClient userServiceClient;
    private final UserSnapshotMapper userSnapshotMapper;

    @Override
    public Optional<UserSnapshot> findByEmail(String email) {
        try {
            OperationResponse<ExternalUserSnapshot> response = userServiceClient.getCredentials(null, email);
            return Optional.ofNullable(userSnapshotMapper.toSnapshot(
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
            return Optional.ofNullable(userSnapshotMapper.toSnapshot(
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
            String email, java.util.Set<com.renewsim.backend.shared.domain.vo.RoleName> roles) {
        com.renewsim.backend.auth_service.application.dto.InternalUserCreateRequest request =
                new com.renewsim.backend.auth_service.application.dto.InternalUserCreateRequest(
                        username, email, rawPassword, fullName);
        OperationResponse<ExternalUserSnapshot> response = userServiceClient.createUser(request);
        return userSnapshotMapper.toSnapshot(response != null ? response.data() : null);
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
            return Optional.ofNullable(userSnapshotMapper.toSnapshot(
                    response != null ? response.data() : null));
        } catch (FeignException.NotFound e) {
            return Optional.empty();
        } catch (FeignException e) {
            log.error("Error fetching user by id={}", userId, e);
            throw e;
        }
    }
}