package com.renewsim.backend.role_service.infrastructure.gateway;

import com.renewsim.backend.role_service.application.port.out.UserServiceGateway;
import com.renewsim.backend.role_service.infrastructure.client.UserServiceClient;
import com.renewsim.backend.user_service.web.dto.UpdateUserRolesRequestDTO;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class HttpUserServiceGateway implements UserServiceGateway {

    private final UserServiceClient userServiceClient;

    @Override
    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackUpdateUserRoles")
    @Retry(name = "userService")
    public void updateUserRoles(Long userId, UpdateUserRolesRequestDTO request) {
        userServiceClient.updateUserRoles(userId, request);
    }

    @Override
    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackAssignRole")
    @Retry(name = "userService")
    public void assignRole(Long userId, Long roleId) {
        userServiceClient.assignRole(userId, roleId);
    }

    @Override
    @CircuitBreaker(name = "userService", fallbackMethod = "fallbackRemoveRole")
    @Retry(name = "userService")
    public void removeRole(Long userId, Long roleId) {
        userServiceClient.removeRole(userId, roleId);
    }

    /**
     * Fallback silencioso: no lanza excepción, solo loguea.
     */
    @SuppressWarnings("unused")
    private void fallbackUpdateUserRoles(Long userId, UpdateUserRolesRequestDTO request, Throwable ex) {
        log.warn("Fallback triggered: Unable to update roles for userId={} -> reason={}", userId, ex.getMessage());
        // no lanzamos excepción → fallback "suave"
    }

    @SuppressWarnings("unused")
    private void fallbackAssignRole(Long userId, Long roleId, Throwable ex) {
        log.warn("Fallback triggered: Unable to assign roleId={} for userId={} -> reason={}", roleId, userId,
                ex.getMessage());
    }

    @SuppressWarnings("unused")
    private void fallbackRemoveRole(Long userId, Long roleId, Throwable ex) {
        log.warn("Fallback triggered: Unable to remove roleId={} for userId={} -> reason={}", roleId, userId,
                ex.getMessage());
    }
}
