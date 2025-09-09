package com.renewsim.backend.auth_service.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.renewsim.backend.auth_service.web.dto.ExternalUserSnapshot;

@FeignClient(name = "user-service", url = "${user.service.url}")
public interface UserServiceClient {

    @GetMapping("/api/v1/users/by-username")
    ExternalUserSnapshot findByUsername(@RequestParam String username);

    @GetMapping("/api/v1/users/exists")
    boolean existsByUsername(@RequestParam String username);

    @PostMapping("/api/v1/users")
    ExternalUserSnapshot createUser(@RequestBody ExternalUserSnapshot request);
}

