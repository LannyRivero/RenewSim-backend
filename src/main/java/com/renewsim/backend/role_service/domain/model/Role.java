package com.renewsim.backend.role_service.domain.model;

import com.renewsim.backend.role_service.domain.exception.InvalidRoleNameException;
import com.renewsim.backend.shared.domain.vo.RoleName;

import java.time.LocalDateTime;
import java.util.Objects;

public record Role(
        Long id,
        RoleName name,
        String description,
        LocalDateTime createdAt) {

    public Role {
        if (name == null) {
            throw new InvalidRoleNameException("RoleName cannot be null");
        }
        description = normalizeDescription(description);
        createdAt = Objects.requireNonNull(createdAt, "CreatedAt cannot be null");
    }

    public Role(RoleName name) {
        this(null, name, null, LocalDateTime.now());
    }

    public Role(RoleName name, String description) {
        this(null, name, description, LocalDateTime.now());
    }

    private static String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }
        String trimmed = description.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Role role))
            return false;
        return name == role.name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "Role{id=" + id + ", name=" + name + ", description='" + description + "', createdAt=" + createdAt + '}';
    }
}
