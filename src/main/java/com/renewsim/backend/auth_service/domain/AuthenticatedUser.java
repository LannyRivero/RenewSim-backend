package com.renewsim.backend.auth_service.domain;

import java.util.Objects;
import java.util.Set;

public record AuthenticatedUser(
                String username,
                Set<String> roles,
                Set<String> scopes) {

        public AuthenticatedUser {
                Objects.requireNonNull(username);
                username = username.trim();
                if (username.isEmpty())
                        throw new IllegalArgumentException("username must not be blank");
                roles = (roles == null) ? Set.of() : Set.copyOf(roles);
                scopes = (scopes == null) ? Set.of() : Set.copyOf(scopes);
        }

        public static AuthenticatedUser of(String username, Set<String> roles, Set<String> scopes) {
                return new AuthenticatedUser(username, roles, scopes);
        }
}