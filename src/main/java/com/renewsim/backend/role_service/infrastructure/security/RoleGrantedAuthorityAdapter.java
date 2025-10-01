package com.renewsim.backend.role_service.infrastructure.security;

import org.springframework.security.core.GrantedAuthority;

import com.renewsim.backend.role_service.domain.model.Role;

public class RoleGrantedAuthorityAdapter implements GrantedAuthority {

    private final Role role;

    public RoleGrantedAuthorityAdapter(Role role) {
        this.role = role;
    }

    @Override
    public String getAuthority() {
        return role.name().asAuthority();
    }

    public Role getRole() {
        return role;
    }

    @Override
    public String toString() {
        return getAuthority();
    }

}
