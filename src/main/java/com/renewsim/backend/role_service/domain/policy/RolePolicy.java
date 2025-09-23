package com.renewsim.backend.role_service.domain.policy;

import com.renewsim.backend.role_service.domain.model.RoleName;

public final class RolePolicy {

    private RolePolicy() {}

    public static RoleName normalizeRoleName(String raw){
        if (raw == null) {
            throw new IllegalArgumentException(" Role name cannot be null");
            
        }
        
        return RoleName.valueOf(raw.trim().toUpperCase());
    }

}
