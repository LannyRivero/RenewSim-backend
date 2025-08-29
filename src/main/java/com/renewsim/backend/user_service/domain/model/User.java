package com.renewsim.backend.user_service.domain.model;

import java.time.Instant;
import java.util.Set;

public record User(
    Long id,
    String username,
    String email,
    boolean enabled,
    Set<String> roles,
    Instant createdAt,
    Instant updatedAt
) {}

