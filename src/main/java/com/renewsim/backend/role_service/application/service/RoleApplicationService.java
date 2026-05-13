package com.renewsim.backend.role_service.application.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.renewsim.backend.role_service.application.command.CreateRoleCommand;
import com.renewsim.backend.role_service.application.port.in.CreateRoleUseCase;
import com.renewsim.backend.role_service.application.port.in.DeleteRoleUseCase;
import com.renewsim.backend.role_service.application.port.in.ExistsRoleUseCase;
import com.renewsim.backend.role_service.application.port.in.GetRolesUseCase;
import com.renewsim.backend.role_service.application.port.out.RoleRepositoryPort;
import com.renewsim.backend.role_service.application.result.RoleCreationResultDTO;
import com.renewsim.backend.role_service.application.result.RoleDeletionResultDTO;
import com.renewsim.backend.role_service.domain.model.Role;
import com.renewsim.backend.shared.domain.vo.RoleName;
import com.renewsim.backend.role_service.domain.policy.RolePolicy;
import com.renewsim.backend.role_service.domain.service.RoleDomainService;
import com.renewsim.backend.role_service.web.dto.RoleDTO;
import com.renewsim.backend.role_service.application.mapper.RoleDtoMapper;

@Service
@Transactional
public class RoleApplicationService implements
        CreateRoleUseCase,
        GetRolesUseCase,
        ExistsRoleUseCase,
        DeleteRoleUseCase {

    private final RoleRepositoryPort roleRepositoryPort;
    private final RoleDomainService roleDomainService;
    private final RoleDtoMapper roleDtoMapper;

    public RoleApplicationService(RoleRepositoryPort roleRepositoryPort,
            RoleDomainService roleDomainService,
            RoleDtoMapper roleDtoMapper) {
        this.roleRepositoryPort = roleRepositoryPort;
        this.roleDomainService = roleDomainService;
        this.roleDtoMapper = roleDtoMapper;
    }

    @Override
    public RoleCreationResultDTO createRole(CreateRoleCommand command) {
        RoleName roleName = RolePolicy.normalizeRoleName(command.name());
        roleDomainService.ensureRoleDoesNotExist(roleName);

        Role role = roleDomainService.createRole(command.name(), command.description());
        Role saved = roleRepositoryPort.save(role);

        return new RoleCreationResultDTO(saved.name().name(), "Role created successfully");
    }

    @Override
    public List<RoleDTO> getAll() {
        return roleRepositoryPort.findAll()
                .stream()
                .map(roleDtoMapper::toDTO)
                .toList();
    }

    @Override
    public boolean existsByName(RoleName roleName) {
        return roleRepositoryPort.findByName(roleName).isPresent();
    }

    @Override
    public RoleDeletionResultDTO delete(Long roleId) {
        Role role = roleDomainService.ensureRoleExists(roleId);

        roleDomainService.ensureNotRemovingLastAdmin(role.name());
        roleRepositoryPort.deleteById(roleId);

        return new RoleDeletionResultDTO(roleId, true, "Role deleted successfully");
    }

}
