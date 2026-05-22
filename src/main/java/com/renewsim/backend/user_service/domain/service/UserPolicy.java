package com.renewsim.backend.user_service.domain.service;

import java.util.Collection;
import java.util.Locale;

import com.renewsim.backend.shared.domain.vo.RoleName;
import com.renewsim.backend.shared.exception.ConflictException;
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

    public static void ensureAtLeastOneAdminRemaining(Collection<RoleName> currentRoles,
            Collection<RoleName> newRoles,
            long totalAdmins) {
        boolean removingAdmin = currentRoles.contains(RoleName.ADMIN) && !newRoles.contains(RoleName.ADMIN);
        if (removingAdmin && totalAdmins <= 1) {
            throw new ConflictException(
                    "Cannot remove ADMIN role: at least one administrator must remain in the system");
        }
    }

    public static void ensureRoleAssignableToUser(RoleName roleName) {
        if (roleName == RoleName.SERVICE_AUTH) {
            throw new InvalidUserDataException("Infrastructure roles cannot be assigned to users");
        }
    }
}

