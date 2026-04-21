package com.renewsim.backend.testutil.mothers;

import com.renewsim.backend.auth_service.web.dto.UserSnapshot;
import com.renewsim.backend.shared.domain.vo.RoleName;

import java.util.Set;

public final class UserSnapshotMother {

    private static final String DEFAULT_HASH = "$2a$10$dummyHashxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";

    private UserSnapshotMother() {
    }

    public static UserSnapshot activeUser(String username, Set<RoleName> roles) {
        return UserSnapshot.active(1L, username, "Full Name", DEFAULT_HASH,
                username + "@example.com", roles);
    }

    public static UserSnapshot disabledUser(String username, Set<RoleName> roles) {
        return UserSnapshot.disabled(1L, username, "Full Name", DEFAULT_HASH,
                username + "@example.com", roles);
    }

    public static UserSnapshot withEmail(String email, Set<RoleName> roles) {
        String username = email.split("@")[0];
        return UserSnapshot.active(1L, username, "Full Name", DEFAULT_HASH, email, roles);
    }
}