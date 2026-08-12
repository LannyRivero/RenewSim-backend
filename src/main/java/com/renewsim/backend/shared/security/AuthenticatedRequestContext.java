package com.renewsim.backend.shared.security;

public record AuthenticatedRequestContext(
        String username,
        boolean isAdmin) {
}
