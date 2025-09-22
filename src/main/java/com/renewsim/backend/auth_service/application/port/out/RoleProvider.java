package com.renewsim.backend.auth_service.application.port.out;

import com.renewsim.backend.role_service.domain.model.RoleName;

public interface RoleProvider {
    
    RoleName defaultRole();

}
