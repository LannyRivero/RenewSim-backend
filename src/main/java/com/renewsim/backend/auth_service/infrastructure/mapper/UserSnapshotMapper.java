package com.renewsim.backend.auth_service.infrastructure.mapper;

import com.renewsim.backend.auth_service.application.dto.UserSnapshot;
import com.renewsim.backend.auth_service.domain.model.AuthUserStatus;
import com.renewsim.backend.auth_service.infrastructure.client.ExternalUserSnapshot;
import com.renewsim.backend.shared.domain.vo.RoleName;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class UserSnapshotMapper {

    public UserSnapshot toSnapshot(ExternalUserSnapshot external) {
        if (external == null)
            return null;

        Set<RoleName> roles = Optional.ofNullable(external.roles())
                .orElse(Collections.emptySet())
                .stream()
                .map(RoleName::valueOf)
                .collect(Collectors.toUnmodifiableSet());

        AuthUserStatus authStatus = mapToAuthUserStatus(external.status());
        boolean enabled = authStatus == AuthUserStatus.ACTIVE;

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

    /**
     * Maps external user status string to internal AuthUserStatus.
     * This decouples the bounded contexts.
     */
    private AuthUserStatus mapToAuthUserStatus(String externalStatus) {
        if (externalStatus == null)
            return AuthUserStatus.INACTIVE;

        return switch (externalStatus) {
            case "ACTIVE" -> AuthUserStatus.ACTIVE;
            case "SUSPENDED" -> AuthUserStatus.SUSPENDED;
            default -> {
                log.warn("Unknown external status value: {}", externalStatus);
                yield AuthUserStatus.INACTIVE;
            }
        };
    }
}
