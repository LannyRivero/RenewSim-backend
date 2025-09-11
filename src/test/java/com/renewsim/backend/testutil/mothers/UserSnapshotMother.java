package com.renewsim.backend.testutil.mothers;

import com.renewsim.backend.auth_service.web.dto.UserSnapshot;
import com.renewsim.backend.role.RoleName;

import java.util.Set;

/**
 * Test mother for UserSnapshot objects.
 */
public final class UserSnapshotMother {

    private static final String DEFAULT_HASH = "$2a$10$dummyHash";

    private UserSnapshotMother() {
    }

    public static UserSnapshot activeUser(String username, Set<RoleName> roles) {
        return UserSnapshot.active(username, username + "@example.com", DEFAULT_HASH, roles);
    }

    public static UserSnapshot disabledUser(String username, Set<RoleName> roles) {
        return UserSnapshot.disabled(username, username + "@example.com", DEFAULT_HASH, roles);
    }
}

