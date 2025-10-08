package com.renewsim.backend.user_service.infraestructure.persistence.adapter;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.renewsim.backend.role_service.domain.model.RoleName;
import com.renewsim.backend.role_service.dto.RoleDTO;
import com.renewsim.backend.shared.dto.OperationResponse;
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
        OperationResponse<Boolean> response = roleServiceClient.existsRole(roleName.name());
        
        return Optional.ofNullable(response)
                .map(OperationResponse::data)
                .orElse(false);
    }

    @Override
    public Optional<RoleSnapshot> findByName(String name) {
        OperationResponse<RoleDTO> response = roleServiceClient.findByName(name);

        return Optional.ofNullable(response)
                .map(OperationResponse::data)
                .map(dto -> new RoleSnapshot(dto.id(), dto.name()));
    }

}