package com.renewsim.backend.auth_service.domain.model;

import java.time.LocalDateTime;
import java.time.Clock;
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
    private final boolean revoked;
    private final LocalDateTime revokedAt;

    private RefreshToken(
            Long id,
            Long userId,
            String tokenHash,
            LocalDateTime issuedAt,
            LocalDateTime expiresAt,
            boolean revoked,
            LocalDateTime revokedAt) {
        this.id = id;
        this.userId = Objects.requireNonNull(userId, "userId cannot be null");
        this.tokenHash = requireNonBlank(tokenHash, "tokenHash");
        this.issuedAt = Objects.requireNonNull(issuedAt, "issuedAt cannot be null");
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt cannot be null");
        if (!expiresAt.isAfter(issuedAt)) {
            throw new IllegalArgumentException("expiresAt must be after issuedAt");
        }
        this.revoked = revoked;
        this.revokedAt = revokedAt;
    }

    /**
     * Factory method for issuing a new refresh token.
     * TTL is 7 days from issuedAt.
     */
    public static RefreshToken issue(Long userId, String tokenHash) {
        return issue(userId, tokenHash, Clock.systemDefaultZone());
    }

    public static RefreshToken issue(Long userId, String tokenHash, Clock clock) {
        LocalDateTime now = LocalDateTime.now(clock);
        return new RefreshToken(null, userId, tokenHash, now, now.plusDays(7), false, null);
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
            boolean revoked,
            LocalDateTime revokedAt) {
        return new RefreshToken(id, userId, tokenHash, issuedAt, expiresAt, revoked, revokedAt);
    }

    /**
     * Returns true if the token is still within its validity window.
     * 
     * @param clock Clock to determine current time
     */
    public boolean isValid(Clock clock) {
        return !revoked && LocalDateTime.now(clock).isBefore(expiresAt);
    }

    /**
     * Factory method that returns a new RefreshToken with revoked=true.
     * The original token remains unchanged (immutability).
     *
     * @param clock Clock to determine current time
     * @return a new RefreshToken instance with revoked=true
     */
    public RefreshToken revoked(Clock clock) {
        return new RefreshToken(
            this.id,
            this.userId,
            this.tokenHash,
            this.issuedAt,
            this.expiresAt,
            true,
            LocalDateTime.now(clock));
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

    public LocalDateTime getRevokedAt() {
        return revokedAt;
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
                ", revoked=" + revoked + ", revokedAt=" + revokedAt + ", expiresAt=" + expiresAt + "}";
    }
}