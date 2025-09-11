package com.renewsim.backend.auth_service.web.dto;

import java.util.Set;

public record ExternalUserSnapshot(
    String username,
    String passwordHash,
    String email,
    Set<String> roles
) {}

