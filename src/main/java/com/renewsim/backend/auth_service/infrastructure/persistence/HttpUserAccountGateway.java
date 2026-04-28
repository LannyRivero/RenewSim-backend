package com.renewsim.backend.auth_service.infrastructure.persistence;

import com.renewsim.backend.auth_service.application.dto.UserSnapshot;
import com.renewsim.backend.auth_service.application.port.out.UserAccountGateway;
import com.renewsim.backend.auth_service.infrastructure.client.ExternalUserSnapshot;
import com.renewsim.backend.auth_service.infrastructure.client.UserServiceClient;
import com.renewsim.backend.auth_service.infrastructure.mapper.UserSnapshotMapper;
import com.renewsim.backend.shared.dto.OperationResponse;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
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

    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackFindByEmail")
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

    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackFindByUsername")
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

    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackExistsByUsername")
    @Override
    public boolean existsByUsername(String username) {
        OperationResponse<Boolean> response = userServiceClient.existsByUsernameOrEmail(username, null);
        return response != null && Boolean.TRUE.equals(response.data());
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackExistsByEmail")
    @Override
    public boolean existsByEmail(String email) {
        OperationResponse<Boolean> response = userServiceClient.existsByUsernameOrEmail(null, email);
        return response != null && Boolean.TRUE.equals(response.data());
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackCreateUser")
    @Override
    public UserSnapshot createUser(String username, String fullName, String rawPassword,
            String email, java.util.Set<com.renewsim.backend.shared.domain.vo.RoleName> roles) {
        com.renewsim.backend.auth_service.application.dto.InternalUserCreateRequest request =
                new com.renewsim.backend.auth_service.application.dto.InternalUserCreateRequest(
                        username, email, rawPassword, fullName);
        OperationResponse<ExternalUserSnapshot> response = userServiceClient.createUser(request);
        return userSnapshotMapper.toSnapshot(response != null ? response.data() : null);
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackActivateUser")
    @Override
    public void activateUser(Long userId) {
        try {
            userServiceClient.activateUser(userId);
        } catch (FeignException e) {
            log.error("Error activating userId={}", userId, e);
            throw e;
        }
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackFindById")
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

    // Fallback methods
    @SuppressWarnings("unused")
    private Optional<UserSnapshot> fallbackFindByEmail(String email, Throwable t) {
        log.error("Circuit breaker fallback for findByEmail: {}", email, t);
        return Optional.empty();
    }

    @SuppressWarnings("unused")
    private Optional<UserSnapshot> fallbackFindByUsername(String username, Throwable t) {
        log.error("Circuit breaker fallback for findByUsername: {}", username, t);
        return Optional.empty();
    }

    @SuppressWarnings("unused")
    private boolean fallbackExistsByUsername(String username, Throwable t) {
        log.error("Circuit breaker fallback for existsByUsername: {}", username, t);
        return false;
    }

    @SuppressWarnings("unused")
    private boolean fallbackExistsByEmail(String email, Throwable t) {
        log.error("Circuit breaker fallback for existsByEmail: {}", email, t);
        return false;
    }

    @SuppressWarnings("unused")
    private UserSnapshot fallbackCreateUser(String username, String fullName, String rawPassword,
            String email, java.util.Set<com.renewsim.backend.shared.domain.vo.RoleName> roles, Throwable t) {
        log.error("Circuit breaker fallback for createUser: {}", username, t);
        throw new IllegalStateException("User service temporarily unavailable");
    }

    @SuppressWarnings("unused")
    private void fallbackActivateUser(Long userId, Throwable t) {
        log.error("Circuit breaker fallback for activateUser: {}", userId, t);
        throw new IllegalStateException("User service temporarily unavailable");
    }

    @SuppressWarnings("unused")
    private Optional<UserSnapshot> fallbackFindById(Long userId, Throwable t) {
        log.error("Circuit breaker fallback for findById: {}", userId, t);
        return Optional.empty();
    }
}