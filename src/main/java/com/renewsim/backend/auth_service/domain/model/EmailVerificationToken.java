package com.renewsim.backend.auth_service.domain.model;

import java.time.LocalDateTime;

/**
 * Domain entity representing an email verification token.
 * Used to verify user email addresses during registration.
 * 
 * Tokens are single-use and have a configurable expiration time.
 */
public class EmailVerificationToken {

    private Long id;
    private Long userId;
    private String token;
    private LocalDateTime expiresAt;
    private LocalDateTime verifiedAt;
    private LocalDateTime createdAt;

    // Constructor para crear nuevo token (sin ID)
    public EmailVerificationToken(Long userId, String token, LocalDateTime expiresAt) {
        this.userId = userId;
        this.token = token;
        this.expiresAt = expiresAt;
        this.createdAt = LocalDateTime.now();
    }

    // Constructor completo (para reconstrucción desde DB)
    public EmailVerificationToken(Long id, Long userId, String token,
            LocalDateTime expiresAt, LocalDateTime verifiedAt,
            LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.token = token;
        this.expiresAt = expiresAt;
        this.verifiedAt = verifiedAt;
        this.createdAt = createdAt;
    }

    // Constructor vacío (requerido por JPA)
    protected EmailVerificationToken() {
    }

    /**
     * Checks if the token has expired.
     * 
     * @return true if current time is after expiration time
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Checks if the token has already been used for verification.
     * 
     * @return true if verifiedAt is not null
     */
    public boolean isVerified() {
        return verifiedAt != null;
    }

    /**
     * Marks the token as verified with current timestamp.
     */
    public void markAsVerified() {
        this.verifiedAt = LocalDateTime.now();
    }

    /**
     * Validates the token can be used for verification.
     * 
     * @throws IllegalStateException if token is expired or already verified
     */
    public void validateCanBeUsed() {
        if (isVerified()) {
            throw new IllegalStateException("Token has already been used");
        }
        if (isExpired()) {
            throw new IllegalStateException("Token has expired");
        }
    }

    // Getters
    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getToken() {
        return token;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Setters (solo los que tienen sentido modificar)
    public void setId(Long id) {
        this.id = id;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }
}