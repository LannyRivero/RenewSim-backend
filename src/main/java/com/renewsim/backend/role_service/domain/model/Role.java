package com.renewsim.backend.role_service.domain.model;

import org.springframework.security.core.GrantedAuthority;

public record Role(Long id, RoleName name) implements GrantedAuthority {

    @Override
    public String getAuthority() {
        return name.name();
    }
}


