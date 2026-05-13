package com.renewsim.backend.role_service.application.service;

import com.renewsim.backend.role_service.application.command.CreateRoleCommand;
import com.renewsim.backend.role_service.application.port.out.RoleRepositoryPort;
import com.renewsim.backend.role_service.application.result.RoleCreationResultDTO;
import com.renewsim.backend.role_service.domain.model.Role;
import com.renewsim.backend.shared.domain.vo.RoleName;
import com.renewsim.backend.role_service.domain.service.RoleDomainService;
import com.renewsim.backend.role_service.web.dto.RoleDTO;
import com.renewsim.backend.role_service.application.mapper.RoleDtoMapper;
import com.renewsim.backend.shared.exception.RoleAlreadyExistsException;
import com.renewsim.backend.shared.exception.RoleNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RoleServiceImplTest {

    private static final LocalDateTime CREATED_AT = LocalDateTime.parse("2026-01-01T00:00:00");

    @Mock
    private RoleRepositoryPort roleRepositoryPort;
    @Mock
    private RoleDomainService roleDomainService;
    @Mock
    private RoleDtoMapper roleDtoMapper;

    private RoleApplicationService roleService;

    @BeforeEach
    void setUp() {
        roleService = new RoleApplicationService(roleRepositoryPort, roleDomainService, roleDtoMapper);
    }

    @Test
    @DisplayName("create should save role when it does not exist")
    void create_success() {
        CreateRoleCommand command = new CreateRoleCommand("ADMIN");
        Role role = new Role(RoleName.ADMIN);

        doNothing().when(roleDomainService).ensureRoleDoesNotExist(RoleName.ADMIN);
        when(roleRepositoryPort.save(any())).thenReturn(role);
        when(roleDtoMapper.toDTO(any(Role.class)))
                .thenReturn(new RoleDTO(1L, "ADMIN", "Administrator role", CREATED_AT));

        RoleCreationResultDTO result = roleService.createRole(command);

        assertThat(result.roleName()).isEqualTo("ADMIN");
        assertThat(result.message()).contains("Role created successfully");
        verify(roleDomainService).ensureRoleDoesNotExist(RoleName.ADMIN);
        verify(roleRepositoryPort).save(any());
    }

    @Test
    @DisplayName("createRole should throw when role already exists (domain service)")
    void createRole_conflict() {
        doThrow(new RoleAlreadyExistsException("Role already exists: ADMIN"))
                .when(roleDomainService).ensureRoleDoesNotExist(RoleName.ADMIN);

        CreateRoleCommand command = new CreateRoleCommand("ADMIN");

        assertThatThrownBy(() -> roleService.createRole(command))
                .isInstanceOf(RoleAlreadyExistsException.class)
                .hasMessageContaining("Role already exists");

        verify(roleDomainService).ensureRoleDoesNotExist(RoleName.ADMIN);
        verify(roleRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("delete should remove role when exists")
    void delete_success() {
        Role role = new Role(RoleName.ADMIN);
        doNothing().when(roleDomainService).ensureNotRemovingLastAdmin(RoleName.ADMIN);
        when(roleDomainService.ensureRoleExists(1L)).thenReturn(role);

        roleService.delete(1L);

        verify(roleDomainService).ensureRoleExists(1L);
        verify(roleDomainService).ensureNotRemovingLastAdmin(RoleName.ADMIN);
        verify(roleRepositoryPort).deleteById(1L);
    }

    @Test
    @DisplayName("delete should throw when role not found (domain service)")
    void delete_notFound() {
        doThrow(new RoleNotFoundException("Role with id=99 not found"))
                .when(roleDomainService).ensureRoleExists(99L);

        assertThatThrownBy(() -> roleService.delete(99L))
                .isInstanceOf(RoleNotFoundException.class);

        verify(roleDomainService).ensureRoleExists(99L);
        verify(roleRepositoryPort, never()).deleteById(any());
    }

    @Test
    @DisplayName("getAll should return roles list")
    void getAll_success() {
        when(roleRepositoryPort.findAll()).thenReturn(List.of(new Role(RoleName.ADMIN)));
        when(roleDtoMapper.toDTO(any(Role.class))).thenReturn(new RoleDTO(1L, "ADMIN", "Administrator role", CREATED_AT));

        var result = roleService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("ADMIN");

        verify(roleRepositoryPort).findAll();
    }

    @Test
    @DisplayName("existsByName should return true when the role is present")
    void existsByName_present() {
        when(roleRepositoryPort.findByName(RoleName.USER)).thenReturn(java.util.Optional.of(new Role(RoleName.USER)));

        boolean result = roleService.existsByName(RoleName.USER);

        assertThat(result).isTrue();
        verify(roleRepositoryPort).findByName(RoleName.USER);
    }

    @Test
    @DisplayName("existsByName should return false when the role is missing")
    void existsByName_missing() {
        when(roleRepositoryPort.findByName(RoleName.USER)).thenReturn(java.util.Optional.empty());

        boolean result = roleService.existsByName(RoleName.USER);

        assertThat(result).isFalse();
        verify(roleRepositoryPort).findByName(RoleName.USER);
    }
}
