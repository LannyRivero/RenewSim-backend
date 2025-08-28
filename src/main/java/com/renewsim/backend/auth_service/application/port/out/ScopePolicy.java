package com.renewsim.backend.auth_service.application.port.out;

import com.renewsim.backend.role.RoleName;
import java.util.Set;

public interface ScopePolicy {
    Set<String> scopesFor(RoleName roleName);
}

