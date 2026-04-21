package com.renewsim.backend.auth_service.application.port.out;

/**
 * Output port for JWT token blacklist.
 * Used to invalidate access tokens on logout.
 */
public interface TokenBlacklistPort {

    /**
     * Adds a JTI to the blacklist.
     *
     * @param jti       the JWT ID claim
     * @param userId    the user who owns the token
     * @param expiresAt epoch seconds when the token naturally expires
     */
    void blacklist(String jti, Long userId, long expiresAt);

    /**
     * Returns true if the JTI has been blacklisted.
     */
    boolean isBlacklisted(String jti);
}