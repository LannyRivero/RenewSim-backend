package com.renewsim.backend.auth_service.infrastructure.persistence;

import com.renewsim.backend.auth_service.application.port.out.RoleProvider;
import com.renewsim.backend.role_service.domain.model.RoleName;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaRoleProvider implements RoleProvider {
    @Override
    public RoleName defaultRole() {
        return RoleName.USER;
    }    
}
