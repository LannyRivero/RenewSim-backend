package com.renewsim.backend.role_service.infrastructure.gateway;

import com.renewsim.backend.role_service.application.port.out.UserServiceGateway;
import com.renewsim.backend.role_service.infrastructure.client.UserServiceClient;
import com.renewsim.backend.user_service.dto.UpdateUserRolesRequestDTO;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
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

    /**
     * Fallback silencioso: no lanza excepción, solo loguea.
     */
    @SuppressWarnings("unused")
    private void fallbackUpdateUserRoles(Long userId, UpdateUserRolesRequestDTO request, Throwable ex) {
        log.warn("Fallback triggered: Unable to update roles for userId={} -> reason={}", userId, ex.getMessage());
        // no lanzamos excepción → fallback "suave"
    }
}
