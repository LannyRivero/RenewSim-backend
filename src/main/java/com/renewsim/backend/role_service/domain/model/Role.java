package com.renewsim.backend.role_service.domain.model;

import java.util.Objects;
import com.renewsim.backend.shared.domain.vo.RoleName;

import com.renewsim.backend.role_service.domain.exception.InvalidRoleNameException;

public record Role(RoleName name) {

    public Role {
        if (name == null) {
            throw new InvalidRoleNameException("RoleName cannot be null");
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
