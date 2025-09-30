package com.renewsim.backend.role_service.domain.policy;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.renewsim.backend.role_service.domain.model.RoleName;

public final class RolePolicy {

    private RolePolicy() {
    }

    public static RoleName normalizeRoleName(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("Role name cannot be null or blank");
        }
        return RoleName.valueOf(raw.trim().toUpperCase());
    }

    /** Ensure no duplicate roles are present in a user. */
    public static void ensureNoDuplicateRoles(Collection<RoleName> roles) {
        if (roles == null)
            return;
        if (roles.size() != new HashSet<>(roles).size()) {
            throw new IllegalArgumentException("Duplicate roles are not allowed");
        }
    }

    /** Rule: USER cannot create or assign ADMIN roles. */
    public static void ensureUserCannotAssignAdmin(RoleName requester, RoleName target) {
        if (requester == RoleName.USER && target == RoleName.ADMIN) {
            throw new SecurityException("USER cannot assign ADMIN role");
        }
    }

    /** Ensure that the system always keeps at least one ADMIN. */
    public static void ensureAtLeastOneAdminRemaining(Set<RoleName> currentRoles,
            Set<RoleName> newRoles,
            long totalAdmins) {
        boolean removingAdmin = currentRoles.contains(RoleName.ADMIN) && !newRoles.contains(RoleName.ADMIN);
        if (removingAdmin && totalAdmins <= 1) {
            throw new IllegalStateException(
                    "Cannot remove ADMIN role: at least one administrator must remain in the system");
        }
    }
}
