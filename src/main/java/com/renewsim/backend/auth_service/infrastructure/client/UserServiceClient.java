package com.renewsim.backend.auth_service.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.renewsim.backend.auth_service.web.dto.ExternalUserSnapshot;

@FeignClient(name = "user-service", url = "${user-service.url}", configuration = FeignAuthConfig.class)
public interface UserServiceClient {

    @GetMapping("/api/v1/users/internal/credentials")
    ExternalUserSnapshot getCredentials(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email);

    @GetMapping("/api/v1/users/by-username")
    ExternalUserSnapshot findByUsername(@RequestParam String username);

    @GetMapping("/api/v1/users/by-email")
    ExternalUserSnapshot findByEmail(@RequestParam String email);

    @GetMapping("/api/v1/users/exists")
    boolean existsByUsernameOrEmail(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email);

    @PostMapping("/api/v1/users")
    ExternalUserSnapshot createUser(@RequestBody ExternalUserSnapshot request);

}
