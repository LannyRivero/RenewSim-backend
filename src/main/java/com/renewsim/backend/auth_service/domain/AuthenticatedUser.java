package com.renewsim.backend.auth_service.domain;

import java.util.Objects;
import java.util.Set;

public record AuthenticatedUser(
                String username,
                Set<String> roles,
                Set<String> scopes) {

        public AuthenticatedUser {
                Objects.requireNonNull(username);
                Objects.requireNonNull(roles);
                Objects.requireNonNull(scopes);
                username = username.trim();
                if (username.isEmpty())
                        throw new IllegalArgumentException("username must not be blank");
                roles = Set.copyOf(roles);
                scopes = Set.copyOf(scopes);
                if (roles.isEmpty())
                        throw new IllegalArgumentException("roles must not be empty");
        }

        /**
         * Convenience factory used when userId is known (e.g. after step2 login).
         */
        public static AuthenticatedUser of(String username, Set<String> roles, Set<String> scopes) {
                return new AuthenticatedUser(username, roles, scopes);
        }
}