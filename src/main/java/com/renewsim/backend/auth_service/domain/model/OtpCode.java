package com.renewsim.backend.auth_service.domain.model;

import java.time.LocalDateTime;
import java.time.Clock;
import java.util.Objects;

/**
 * OTP code entity.
 * Represents a one-time password issued for a specific purpose (LOGIN, etc.).
 * Pure domain object — no framework dependencies.
 * 
 * This class is IMMUTABLE. All state-changing operations return new instances.
 */
public class OtpCode {

    public enum Purpose {
        LOGIN,
        PASSWORD_RESET,
        EMAIL_VERIFICATION
    }

    private final Long id;
    private final Long userId;
    private final String codeHash;
    private final Purpose purpose;
    private final LocalDateTime issuedAt;
    private final LocalDateTime expiresAt;
    private final boolean used; // CAMBIO: ahora es final

    private OtpCode(
            Long id,
            Long userId,
            String codeHash,
            Purpose purpose,
            LocalDateTime issuedAt,
            LocalDateTime expiresAt,
            boolean used) {
        this.id = id;
        this.userId = Objects.requireNonNull(userId, "userId cannot be null");
        this.codeHash = requireNonBlank(codeHash, "codeHash");
        this.purpose = Objects.requireNonNull(purpose, "purpose cannot be null");
        this.issuedAt = Objects.requireNonNull(issuedAt, "issuedAt cannot be null");
        this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt cannot be null");
        if (!expiresAt.isAfter(issuedAt)) {
            throw new IllegalArgumentException("expiresAt must be after issuedAt");
        }
        this.used = used;
    }

    /**
     * Factory method for issuing a new OTP code.
     * TTL is 5 minutes from issuedAt.
     */
    public static OtpCode issue(Long userId, String codeHash, Purpose purpose) {
        return issue(userId, codeHash, purpose, Clock.systemDefaultZone());
    }

    public static OtpCode issue(Long userId, String codeHash, Purpose purpose, Clock clock) {
        LocalDateTime now = LocalDateTime.now(clock);
        return new OtpCode(null, userId, codeHash, purpose, now, now.plusMinutes(5), false);
    }

    /**
     * Factory method for reconstituting from persistence.
     */
    public static OtpCode reconstitute(
            Long id,
            Long userId,
            String codeHash,
            Purpose purpose,
            LocalDateTime issuedAt,
            LocalDateTime expiresAt,
            boolean used) {
        return new OtpCode(id, userId, codeHash, purpose, issuedAt, expiresAt, used);
    }

    /**
     * Returns true if the OTP has passed its expiration time.
     * 
     * @param clock Clock to determine current time
     */
    public boolean isExpired(Clock clock) {
        return LocalDateTime.now(clock).isAfter(expiresAt);
    }

    /**
     * Returns true if the OTP can still be used: not consumed and not expired.
     * 
     * @param clock Clock to determine current time
     */
    public boolean isValid(Clock clock) {
        return !used && !isExpired(clock);
    }

    /**
     * Returns a new OtpCode with used=true.
     * The original instance remains unchanged (immutability).
     *
     * @return a new OtpCode instance with used=true
     */
    public OtpCode markUsed() {
        return new OtpCode(
                this.id,
                this.userId,
                this.codeHash,
                this.purpose,
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

    public String getCodeHash() {
        return codeHash;
    }

    public Purpose getPurpose() {
        return purpose;
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
        return "OtpCode{id=" + id + ", userId=" + userId +
                ", purpose=" + purpose + ", used=" + used +
                ", expiresAt=" + expiresAt + "}";
    }
}