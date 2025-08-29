package com.renewsim.backend.user_service.domain.service;

public final class UserPolicy {

    private UserPolicy() {
    }

    public static String normalizeUsername(String raw) {
        if (raw == null) {
            return null;
        }
        return raw.trim().toLowerCase();
    }
}
