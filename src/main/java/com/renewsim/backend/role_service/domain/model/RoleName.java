package com.renewsim.backend.role_service.domain.model;

public enum RoleName {
    USER("User"),
    ADMIN("Administrator"),
    SERVICE_AUTH("Service Auth");

    private final String displayName;

    RoleName(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns the authority name following Spring Security convention.
     * Example: ROLE_USER, ROLE_ADMIN, ROLE_SERVICE_AUTH
     */
    public String asAuthority() {
        return "ROLE_" + this.name();
    }
}

