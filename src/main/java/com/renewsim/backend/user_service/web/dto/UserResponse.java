package com.renewsim.backend.user_service.web.dto;

import java.time.Instant;
import java.util.Set;

public record UserResponse(
        Long id,
        String username,
        String email,
        String fullName,
        String phone,
        String status,
        Set<String> roles,
        Instant createdAt,
        Instant activatedAt
) {}