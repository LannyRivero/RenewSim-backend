package com.renewsim.backend.user_service.domain.model;

import com.renewsim.backend.role_service.domain.model.RoleName;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;

public record User(
        Long id,
        String username,
        String email,
        boolean enabled,
        Set<RoleName> roles,
        Instant createdAt,
        Instant updatedAt,
        String passwordHash) {

    public User {
        // Defensive copy + fallback
        roles = (roles == null) ? Set.of() : Set.copyOf(roles);

        // Invariants
        Objects.requireNonNull(username, "Username cannot be null");
        Objects.requireNonNull(email, "Email cannot be null");
        Objects.requireNonNull(passwordHash, "Password cannot be null");

        if (username.isBlank()) {
            throw new IllegalArgumentException("Username must not be blank");
        }
        if (passwordHash.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }
    }

    public static User create(String username, String email, String passwordHash, Set<RoleName> roles) {
        return new User(
                null,
                username,
                email,
                true, 
                roles,
                Instant.now(),
                Instant.now(),
                passwordHash
        );
    }
    public User withAdditionalRole(RoleName role) {
        Set<RoleName> updated = new java.util.HashSet<>(this.roles);
        updated.add(role);
        return new User(id, username, email, enabled, updated, createdAt, Instant.now(), passwordHash);
    }

    public User withoutRole(RoleName role) {
        Set<RoleName> updated = new java.util.HashSet<>(this.roles);
        updated.remove(role);
        return new User(id, username, email, enabled, updated, createdAt, Instant.now(), passwordHash);
    }
}
