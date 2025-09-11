package com.renewsim.backend.role;

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
}
