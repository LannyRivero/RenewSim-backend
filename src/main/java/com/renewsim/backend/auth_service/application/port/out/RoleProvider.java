package com.renewsim.backend.auth_service.application.port.out;

import com.renewsim.backend.role.RoleName;

public interface RoleProvider {
    
    RoleName defaultRole();

}
