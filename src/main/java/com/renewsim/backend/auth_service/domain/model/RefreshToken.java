package com.renewsim.backend.auth_service.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Refresh token entity.
 * Represents a long-lived token used to rotate JWT access tokens.
 * Pure domain object — no framework dependencies.
 */
public class RefreshToken {

    private final Long id;
    private final Long userId;
    private final String tokenHash;
    private final LocalDateTime issuedAt;
    private final LocalDateTime expiresAt;
    private boolean revoked;

    private RefreshToken(
            Long id,
            Long userId,
            String tokenHash,
            LocalDateTime issuedAt,
            LocalDateTime expiresAt,
            boolean revoked) {
        this.id = id;
        this.userId = Objects.requireNonNull(userId, "userId cannot be null");
        this.tokenHash = requireNonBlank(tokenHash, "tokenHash");
        this.issuedAt = Objects.requireNonNull(issuedAt, "issuedAt cannot be null");
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt cannot be null");
        if (!expiresAt.isAfter(issuedAt)) {
            throw new IllegalArgumentException("expiresAt must be after issuedAt");
        }
        this.revoked = revoked;
    }

    /**
     * Factory method for issuing a new refresh token.
     * TTL is 7 days from issuedAt.
     */
    public static RefreshToken issue(Long userId, String tokenHash) {
        LocalDateTime now = LocalDateTime.now();
        return new RefreshToken(null, userId, tokenHash, now, now.plusDays(7), false);
    }

    /**
     * Factory method for reconstituting from persistence.
     */
    public static RefreshToken reconstitute(
            Long id,
            Long userId,
            String tokenHash,
            LocalDateTime issuedAt,
            LocalDateTime expiresAt,
            boolean revoked) {
        return new RefreshToken(id, userId, tokenHash, issuedAt, expiresAt, revoked);
    }

    /**
     * Returns true if the token is still within its validity window.
     */
    public boolean isValid() {
        return !revoked && LocalDateTime.now().isBefore(expiresAt);
    }

    /**
     * Revokes this refresh token permanently.
     * Idempotent: calling twice has no additional effect.
     */
    public void revoke() {
        this.revoked = true;
    }

    // --- Getters ---

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public boolean isRevoked() {
        return revoked;
    }

    // --- Helpers ---

    private static String requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " cannot be null or blank");
        }
        return value;
    }

    @Override
    public String toString() {
        return "RefreshToken{id=" + id + ", userId=" + userId +
                ", revoked=" + revoked + ", expiresAt=" + expiresAt + "}";
    }
}