package com.renewsim.backend.auth_service.infrastructure.mapper;

import com.renewsim.backend.auth_service.application.dto.UserSnapshot;
import com.renewsim.backend.auth_service.domain.model.AuthUserStatus;
import com.renewsim.backend.auth_service.infrastructure.client.ExternalUserSnapshot;
import com.renewsim.backend.shared.domain.vo.RoleName;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper for converting ExternalUserSnapshot to internal UserSnapshot.
 * Decouples the mapping logic from the gateway implementation.
 */
@Component
public class UserSnapshotMapper {

    public UserSnapshot toSnapshot(ExternalUserSnapshot external) {
        if (external == null)
            return null;

        Set<RoleName> roles = Optional.ofNullable(external.roles())
                .orElse(Collections.emptySet())
                .stream()
                .map(RoleName::valueOf)
                .collect(Collectors.toUnmodifiableSet());

        AuthUserStatus authStatus = external.status() == null
                ? AuthUserStatus.INACTIVE
                : switch (external.status()) {
                    case "ACTIVE" -> AuthUserStatus.ACTIVE;
                    case "SUSPENDED" -> AuthUserStatus.SUSPENDED;
                    default -> AuthUserStatus.INACTIVE;
                };

        // User is enabled if status is ACTIVE
        // If emailVerified field is present, it must also be true
        // If emailVerified field is absent (null), we assume email is verified for
        // ACTIVE users
        boolean enabled = authStatus == AuthUserStatus.ACTIVE
                && (external.emailVerified() == null || Boolean.TRUE.equals(external.emailVerified()));
        return new UserSnapshot(
                external.id(),
                external.username(),
                external.fullName(),
                external.passwordHash(),
                external.email(),
                roles,
                authStatus,
                enabled);
    }
}