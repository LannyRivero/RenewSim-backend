package com.renewsim.backend.user_service.domain.service;

import java.util.Locale;

import com.renewsim.backend.shared.exception.InvalidUserDataException;

public final class UserPolicy {

    private UserPolicy() {}

    public static String normalizeUsername(String raw) {
        return raw == null ? null : raw.trim().toLowerCase(Locale.ROOT);
    }

    public static String normalizeEmail(String raw) {
        return raw == null ? null : raw.trim().toLowerCase(Locale.ROOT);
    }

    public static void validatePaswordStrength(String password) {
        if (password == null || !password.matches("^(?=.*[A-Z])(?=.*[0-9]).{8,}$")) {
             throw new InvalidUserDataException("Password must contain at least 1 uppercase letter and 1 number");
        }
    }
}

