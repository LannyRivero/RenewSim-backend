package com.renewsim.backend.user_service.infraestructure.persistence.adapter;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.renewsim.backend.role_service.domain.model.RoleName;
import com.renewsim.backend.user_service.application.port.out.RoleCatalogPort;
import com.renewsim.backend.user_service.dto.RoleSnapshot;
import com.renewsim.backend.user_service.infraestructure.client.RoleServiceClient;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RoleCatalogFeignAdapter implements RoleCatalogPort {

    private final RoleServiceClient roleServiceClient;

    @Override
    public boolean existsByName(RoleName roleName) {
        return roleServiceClient.existsRole(roleName.name());
    }

  @Override
    public Optional<RoleSnapshot> findByName(String name) {
        var dto = roleServiceClient.findByName(name);
        return Optional.ofNullable(new RoleSnapshot(dto.id(), dto.name()));
    }

 }