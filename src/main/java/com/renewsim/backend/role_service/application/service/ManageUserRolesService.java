package com.renewsim.backend.role_service.application.service;

import com.renewsim.backend.role_service.application.command.ManageUserRolesCommand;
import com.renewsim.backend.role_service.application.port.in.ManageUserRolesUseCase;
import com.renewsim.backend.role_service.application.port.out.UserServiceGateway;
import com.renewsim.backend.role_service.application.result.ManageUserRolesResultDTO;
import com.renewsim.backend.user_service.dto.UpdateUserRolesRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ManageUserRolesService implements ManageUserRolesUseCase {

    private final UserServiceGateway userServiceGateway;

    @Override
    public ManageUserRolesResultDTO manageRoles(ManageUserRolesCommand command) {
        // unir asignaciones y revocaciones (ejemplo simple: la request final incluye
        // todos los roles asignados válidos)
        List<String> finalRoles = command.rolesToAssign().stream()
                .map(r -> "ROLE_" + r)
                .toList();

        UpdateUserRolesRequestDTO request = new UpdateUserRolesRequestDTO(finalRoles);

        userServiceGateway.updateUserRoles(command.targetUserId(), request);

        return new ManageUserRolesResultDTO(
                command.targetUserId(),
                finalRoles,
                command.rolesToRevoke().stream().map(r -> "ROLE_" + r).toList(),
                true,
                "User roles updated in batch");
    }
}
