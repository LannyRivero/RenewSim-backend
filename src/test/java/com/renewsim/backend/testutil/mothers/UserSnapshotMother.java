package com.renewsim.backend.testutil.mothers;

import com.renewsim.backend.auth_service.web.dto.UserSnapshot;
import com.renewsim.backend.role.RoleName;

import java.util.Set;

/**
 * Test data factory for creating UserSnapshot instances in unit tests.
 * Provides convenience methods to avoid constructor argument ordering mistakes
 * and improve test readability.
 */
public final class UserSnapshotMother {

    private static final String DEFAULT_PASSWORD_HASH = "$2a$10$dummyHash";

    private UserSnapshotMother() {
    }

    public static UserSnapshot activeUser(String username, Set<RoleName> roles) {
        return UserSnapshot.active(username, DEFAULT_PASSWORD_HASH, roles);
    }

    public static UserSnapshot disabledUser(String username, Set<RoleName> roles) {
        return UserSnapshot.disabled(username, DEFAULT_PASSWORD_HASH, roles);
    }

    public static UserSnapshot withCustomPassword(String username, String passwordHash, Set<RoleName> roles, boolean enabled) {
        return new UserSnapshot(username, passwordHash, roles, enabled);
    }
}
