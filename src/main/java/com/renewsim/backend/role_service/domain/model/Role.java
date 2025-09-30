package com.renewsim.backend.role_service.domain.model;

import org.springframework.security.core.GrantedAuthority;

public record Role(Long id, RoleName name) implements GrantedAuthority {

    public Role {
        if(id != null && id <= 0) {
            throw new IllegalArgumentException("Role ID must be positive if provided");
        }
        if (name == null) {
            throw new IllegalArgumentException("RoleName cannot be null");

        }
    }

    @Override
    public String getAuthority() {
        return name.asAuthority();
    }
}
