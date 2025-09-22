package com.renewsim.backend.user_service.infraestructure.persistence.adapter;

import com.renewsim.backend.role_service.domain.model.RoleName;
import com.renewsim.backend.user_service.application.port.out.RoleCatalogPort;
import com.renewsim.backend.user_service.infraestructure.client.RoleServiceClient;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoleCatalogFeignAdapter implements RoleCatalogPort {

    private final RoleServiceClient roleServiceClient;

    @Override
    public boolean existsByName(RoleName roleName) {
        return roleServiceClient.existsRole(roleName.name());
    }
}

