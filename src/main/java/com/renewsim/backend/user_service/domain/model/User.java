package com.renewsim.backend.user_service.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;

public record User(
        Long id,
        String username,
        String email,
        boolean enabled,
        Set<String> roles,
        Instant createdAt,
        Instant updatedAt,
        String passwordHash) {
    public User {
        if (roles != null) {
            roles = Set.copyOf(roles);
        }

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

    public static User create(String username, String email, String passwordHash, Set<String> roles) {
        return new User(
                null,
                username,
                email,
                true,
                roles != null ? Set.copyOf(roles) : Set.of(),
                Instant.now(),
                Instant.now(),
                passwordHash);

    }
}
