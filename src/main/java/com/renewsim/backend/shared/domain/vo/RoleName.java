package com.renewsim.backend.shared.domain.vo;

public enum RoleName {
    USER("User"),
    ANALYST("Analyst"),
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
     * Example: ROLE_USER, ROLE_ANALYST, ROLE_ADMIN, ROLE_SERVICE_AUTH
     */
    public String asAuthority() {
        return "ROLE_" + this.name();
    }

    /**
     * Converts an authority string (e.g., ROLE_ADMIN) back into RoleName enum.
     * Throws IllegalArgumentException if the value is invalid.
     */
    public static RoleName fromAuthority(String authority) {
        if (authority != null && authority.startsWith("ROLE_")) {
            return RoleName.valueOf(authority.substring(5));
        }
        throw new IllegalArgumentException("Invalid authority: " + authority);
    }
}
