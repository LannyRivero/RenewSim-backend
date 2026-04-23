package com.renewsim.backend.auth_service.application.dto;

import com.renewsim.backend.auth_service.domain.model.AuthUserStatus;
import com.renewsim.backend.shared.domain.vo.RoleName;

import java.util.Set;

public record UserSnapshot(
                Long id,
                String username,
                String fullName,
                String passwordHash,
                String email,
                Set<RoleName> roles,
                AuthUserStatus status,
                boolean enabled) {
        public static UserSnapshot active(Long id, String username, String fullName,
                        String passwordHash, String email, Set<RoleName> roles) {
                return new UserSnapshot(id, username, fullName, passwordHash, email, roles,
                                AuthUserStatus.ACTIVE, true);
        }

        public static UserSnapshot disabled(Long id, String username, String fullName,
                        String passwordHash, String email, Set<RoleName> roles) {
                return new UserSnapshot(id, username, fullName, passwordHash, email, roles,
                                AuthUserStatus.INACTIVE, false);
        }
}