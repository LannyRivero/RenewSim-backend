package com.renewsim.backend.user_service.domain.model;

/**
 * User account status in the system.
 * INACTIVE: Registered user pending activation.
 * ACTIVE: User with activated account.
 * SUSPENDED: Temporarily blocked user.
 */
public enum UserStatus {
    INACTIVE,
    ACTIVE,
    SUSPENDED
}