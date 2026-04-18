package com.renewsim.backend.auth_service.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.renewsim.backend.auth_service.web.dto.ExternalUserSnapshot;
import com.renewsim.backend.shared.dto.OperationResponse;
import com.renewsim.backend.user_service.web.dto.UserCreateRequest;

@FeignClient(name = "user-service", contextId = "authUserClient", url = "${user-service.url}", configuration = FeignAuthConfig.class)
public interface UserServiceClient {

        @PostMapping("/api/v1/users")
        OperationResponse<ExternalUserSnapshot> createUser(@RequestBody UserCreateRequest request);

        @GetMapping("/api/v1/users/internal/credentials")
        OperationResponse<ExternalUserSnapshot> getCredentials(
                        @RequestParam(required = false) String username,
                        @RequestParam(required = false) String email);

        @GetMapping("/api/v1/users/by-username")
        OperationResponse<ExternalUserSnapshot> findByUsername(@RequestParam String username);

        @GetMapping("/api/v1/users/by-email")
        OperationResponse<ExternalUserSnapshot> findByEmail(@RequestParam String email);

        @GetMapping("/api/v1/users/exists")
        OperationResponse<Boolean> existsByUsernameOrEmail(
                        @RequestParam(required = false) String username,
                        @RequestParam(required = false) String email);

        @PutMapping("/api/v1/users/{id}/activate")
        OperationResponse<Void> activateUser(@PathVariable("id") Long userId);
}