package com.renewsim.backend.auth_service.web.dto;

import com.renewsim.backend.role.RoleName;
import java.util.Set;

public record UserSnapshot(
    String username,
    String passwordHash,
    Set<RoleName> roles,
    boolean enabled
) {}


