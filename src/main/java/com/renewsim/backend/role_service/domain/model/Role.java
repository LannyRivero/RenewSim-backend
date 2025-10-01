package com.renewsim.backend.role_service.domain.model;

import java.util.Objects;

public record Role(Long id, RoleName name) {

    public Role {
        if (id != null && id <= 0) {
            throw new IllegalArgumentException("Role ID must be positive if provided");
        }
        if (name == null) {
            throw new IllegalArgumentException("RoleName cannot be null");

        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Role))
            return false;
        Role role = (Role) o;
        return name == role.name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Role{name=" + name + '}';
    }
}
