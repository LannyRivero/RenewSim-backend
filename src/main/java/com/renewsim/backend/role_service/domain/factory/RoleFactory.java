package com.renewsim.backend.role_service.domain.factory;

import com.renewsim.backend.role_service.domain.model.Role;
import com.renewsim.backend.shared.domain.vo.RoleName;
import com.renewsim.backend.role_service.domain.policy.RolePolicy;

public final class RoleFactory {

    private RoleFactory() {
    }

    public static Role createRole(String rawName) {
        return createRole(rawName, null);
    }

    public static Role createRole(String rawName, String description) {
        RoleName normalized = RolePolicy.normalizeRoleName(rawName);
        return new Role(normalized, description);
    }
}
