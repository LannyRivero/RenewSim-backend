package com.renewsim.backend.role_service.domain.model;

import org.springframework.security.core.GrantedAuthority;

public record Role(Long id, RoleName name) implements GrantedAuthority {

    public Role {
        if (name == null) {
            throw new IllegalArgumentException("RoleName cannot be null");

        }
    }

    @Override
    public String getAuthority() {
        return name.asAuthority();
    }
}
