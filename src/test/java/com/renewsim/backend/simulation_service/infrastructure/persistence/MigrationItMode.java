package com.renewsim.backend.simulation_service.infrastructure.persistence;

enum MigrationItMode {
    CONTAINER(false, "none"),
    JDBC_FALLBACK(true, "container_unavailable");

    private final boolean fallbackUsed;
    private final String defaultReasonCode;

    MigrationItMode(boolean fallbackUsed, String defaultReasonCode) {
        this.fallbackUsed = fallbackUsed;
        this.defaultReasonCode = defaultReasonCode;
    }

    boolean isFallbackUsed() {
        return fallbackUsed;
    }

    String defaultReasonCode() {
        return defaultReasonCode;
    }
}
