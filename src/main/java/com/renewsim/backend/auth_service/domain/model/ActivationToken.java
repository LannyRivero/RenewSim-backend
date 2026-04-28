package com.renewsim.backend.auth_service.domain.model;

import java.time.LocalDateTime;
import java.time.Clock;
import java.util.Objects;

/**
 * Activation token entity.
 * Represents a single-use token issued to verify a user's email address.
 * Pure domain object — no framework dependencies.
 * 
 * This class is IMMUTABLE. All state-changing operations return new instances.
 */
public class ActivationToken {

    private final Long id;
    private final Long userId;
    private final String tokenHash;
    private final LocalDateTime issuedAt;
    private final LocalDateTime expiresAt;
    private final boolean used;

    private ActivationToken(
            Long id,
            Long userId,
            String tokenHash,
            LocalDateTime issuedAt,
            LocalDateTime expiresAt,
            boolean used) {
        this.id = id;
        this.userId = Objects.requireNonNull(userId, "userId cannot be null");
        this.tokenHash = requireNonBlank(tokenHash, "tokenHash");
        this.issuedAt = Objects.requireNonNull(issuedAt, "issuedAt cannot be null");
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt cannot be null");
        if (!expiresAt.isAfter(issuedAt)) {
            throw new IllegalArgumentException("expiresAt must be after issuedAt");
        }
        this.used = used;
    }

    /**
     * Factory method for issuing a new activation token.
     * TTL is 24 hours from issuedAt.
     */
    public static ActivationToken issue(Long userId, String tokenHash) {
        return issue(userId, tokenHash, Clock.systemDefaultZone());
    }

    public static ActivationToken issue(Long userId, String tokenHash, Clock clock) {
        LocalDateTime now = LocalDateTime.now(clock);
        return new ActivationToken(null, userId, tokenHash, now, now.plusHours(24), false);
    }

    /**
     * Factory method for reconstituting from persistence.
     */
    public static ActivationToken reconstitute(
            Long id,
            Long userId,
            String tokenHash,
            LocalDateTime issuedAt,
            LocalDateTime expiresAt,
            boolean used) {
        return new ActivationToken(id, userId, tokenHash, issuedAt, expiresAt, used);
    }

    /**
     * Returns true if the token has passed its expiration time.
     * 
     * @param clock Clock to determine current time
     */
    public boolean isExpired(Clock clock) {
        return LocalDateTime.now(clock).isAfter(expiresAt);
    }

    /**
     * Returns true if the token can still be used: not consumed and not expired.
     * 
     * @param clock Clock to determine current time
     */
    public boolean isValid(Clock clock) {
        return !used && !isExpired(clock);
    }

    /**
     * Returns a new ActivationToken with used=true.
     * The original instance remains unchanged (immutability).
     *
     * @return a new ActivationToken instance with used=true
     */
    public ActivationToken markUsed() {
        return new ActivationToken(
                this.id,
                this.userId,
                this.tokenHash,
                this.issuedAt,
                this.expiresAt,
                true);
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

    public boolean isUsed() {
        return used;
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
        return "ActivationToken{id=" + id + ", userId=" + userId +
                ", used=" + used + ", expiresAt=" + expiresAt + "}";
    }
}