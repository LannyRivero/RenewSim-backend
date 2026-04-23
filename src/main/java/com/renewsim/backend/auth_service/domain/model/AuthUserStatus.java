package com.renewsim.backend.auth_service.domain.model;

/**
 * User account status from the perspective of the authentication service.
 * This is a bounded context-specific representation, decoupled from user_service.
 * 
 * INACTIVE: User account not yet activated (pending email verification).
 * ACTIVE: User account is active and can authenticate.
 * SUSPENDED: User account is temporarily blocked from authentication.
 */
public enum AuthUserStatus {
    INACTIVE,
    ACTIVE,
    SUSPENDED
}