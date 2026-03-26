package com.renewsim.backend.auth_service.application.port.out;

import com.renewsim.backend.shared.domain.vo.RoleName;

public interface RoleProvider {
    
    RoleName defaultRole();

}
