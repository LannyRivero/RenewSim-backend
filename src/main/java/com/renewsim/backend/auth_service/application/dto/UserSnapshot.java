package com.renewsim.backend.auth_service.application.dto;

import com.renewsim.backend.shared.domain.vo.RoleName;
import com.renewsim.backend.user_service.domain.model.UserStatus;

import java.util.Set;

public record UserSnapshot(
        Long id,
        String username,
        String fullName,
        String passwordHash,
        String email,
        Set<RoleName> roles,
        UserStatus status,
        boolean enabled) {
    public static UserSnapshot active(Long id, String username, String fullName,
            String passwordHash, String email, Set<RoleName> roles) {
        return new UserSnapshot(id, username, fullName, passwordHash, email, roles,
                UserStatus.ACTIVE, true);
    }

    public static UserSnapshot disabled(Long id, String username, String fullName,
            String passwordHash, String email, Set<RoleName> roles) {
        return new UserSnapshot(id, username, fullName, passwordHash, email, roles,
                UserStatus.INACTIVE, false);
    }
}