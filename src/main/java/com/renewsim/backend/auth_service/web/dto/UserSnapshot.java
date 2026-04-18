package com.renewsim.backend.auth_service.web.dto;

import java.util.Set;
import com.renewsim.backend.shared.domain.vo.RoleName;

public record UserSnapshot(
        Long id,
        String username,
        String passwordHash,
        String email,
        Set<RoleName> roles,
        boolean enabled) {

    public static UserSnapshot active(Long id, String username, String passwordHash,
            String email, Set<RoleName> roles) {
        return new UserSnapshot(id, username, passwordHash, email, roles, true);
    }

    public static UserSnapshot disabled(Long id, String username, String passwordHash,
            String email, Set<RoleName> roles) {
        return new UserSnapshot(id, username, passwordHash, email, roles, false);
    }
}