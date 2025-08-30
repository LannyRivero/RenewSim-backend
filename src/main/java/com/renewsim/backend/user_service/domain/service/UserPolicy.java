package com.renewsim.backend.user_service.domain.service;

import java.util.Locale;

public final class UserPolicy {

    private UserPolicy() {
    }

    public static String normalizeUsername(String raw) {
        return raw == null ? null : raw.trim().toLowerCase(Locale.ROOT);
    }

    public static String normalizeEmail(String raw) {
        return raw == null ? null : raw.trim().toLowerCase(Locale.ROOT);
    }
}

