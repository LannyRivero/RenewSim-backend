package com.renewsim.backend.testutil.mothers;

import com.renewsim.backend.auth_service.web.dto.UserSnapshot;
import com.renewsim.backend.shared.domain.vo.RoleName;

import java.util.Set;

/**
 * Test mother for UserSnapshot objects.
 */
public final class UserSnapshotMother {

    private static final String DEFAULT_HASH = "$2a$10$dummyHash";

    private UserSnapshotMother() {
    }

    public static UserSnapshot activeUser(String username, Set<RoleName> roles) {
        Long id = 1L;
        return UserSnapshot.active(id, username, DEFAULT_HASH, username + "@example.com", roles);
    }

    public static UserSnapshot disabledUser(String username, Set<RoleName> roles) {
        Long id = 1L;
        return UserSnapshot.disabled(id, username, DEFAULT_HASH, username + "@example.com", roles);
    }
}

