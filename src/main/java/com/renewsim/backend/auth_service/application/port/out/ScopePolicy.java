package com.renewsim.backend.auth_service.application.port.out;

import java.util.Set;
import java.util.stream.Collectors;

import com.renewsim.backend.shared.domain.vo.RoleName;

public interface ScopePolicy {
    Set<String> scopesFor(RoleName roleName);

    default Set<String> getScopes(Set<RoleName> roles) {
        return roles.stream()
                .flatMap(role -> scopesFor(role).stream())
                .collect(Collectors.toUnmodifiableSet());
    }
}

