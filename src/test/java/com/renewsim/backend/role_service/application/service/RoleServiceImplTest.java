package com.renewsim.backend.role_service.application.service;

import com.renewsim.backend.role_service.application.port.out.RoleRepositoryPort;
import com.renewsim.backend.role_service.domain.model.Role;
import com.renewsim.backend.role_service.domain.model.RoleName;
import com.renewsim.backend.role_service.domain.policy.RoleValidator;
import com.renewsim.backend.role_service.dto.RoleDTO;
import com.renewsim.backend.role_service.infrastructure.mapper.RoleServiceMapperImpl;
import com.renewsim.backend.shared.exception.RoleAlreadyExistsException;
import com.renewsim.backend.shared.exception.RoleNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RoleServiceImplTest {

    private RoleRepositoryPort roleRepositoryPort;
    private RoleValidator roleValidator;
    private RoleServiceImpl roleService;
    private RoleServiceMapperImpl roleServiceMapper;

    @BeforeEach
    void setUp() {
        roleRepositoryPort = mock(RoleRepositoryPort.class);
        roleValidator = mock(RoleValidator.class);
        roleServiceMapper = mock(RoleServiceMapperImpl.class);
        roleService = new RoleServiceImpl(roleRepositoryPort, roleValidator, roleServiceMapper);
    }

    @Test
    @DisplayName("create should save role when it does not exist")
    void create_success() {
        RoleDTO dto = new RoleDTO(null, "ADMIN");
        Role role = new Role(1L, RoleName.ADMIN);

        // validator no lanza excepción
        doNothing().when(roleValidator).validateRoleDoesNotExist(RoleName.ADMIN);
        when(roleRepositoryPort.save(any())).thenReturn(role);
        when(roleServiceMapper.toDTO(any(Role.class)))
                .thenReturn(new RoleDTO(1L, "ADMIN"));

        RoleDTO result = roleService.create(dto);

        assertThat(result.name()).isEqualTo("ADMIN");
        verify(roleValidator).validateRoleDoesNotExist(RoleName.ADMIN);
        verify(roleRepositoryPort).save(any());
    }

    @Test
    @DisplayName("create should throw when role already exists (validator)")
    void create_conflict() {
        doThrow(new RoleAlreadyExistsException("Role already exists: ADMIN"))
                .when(roleValidator).validateRoleDoesNotExist(RoleName.ADMIN);

        RoleDTO dto = new RoleDTO(null, "ADMIN");

        assertThatThrownBy(() -> roleService.create(dto))
                .isInstanceOf(RoleAlreadyExistsException.class)
                .hasMessageContaining("Role already exists");

        verify(roleValidator).validateRoleDoesNotExist(RoleName.ADMIN);
        verify(roleRepositoryPort, never()).save(any());
    }

    @Test
    @DisplayName("delete should remove role when exists")
    void delete_success() {
        doNothing().when(roleValidator).validateRoleExists(1L);

        roleService.delete(1L);

        verify(roleValidator).validateRoleExists(1L);
        verify(roleRepositoryPort).deleteById(1L);
    }

    @Test
    @DisplayName("delete should throw when role not found (validator)")
    void delete_notFound() {
        doThrow(new RoleNotFoundException("Role with id=99 not found"))
                .when(roleValidator).validateRoleExists(99L);

        assertThatThrownBy(() -> roleService.delete(99L))
                .isInstanceOf(RoleNotFoundException.class);

        verify(roleValidator).validateRoleExists(99L);
        verify(roleRepositoryPort, never()).deleteById(any());
    }

    @Test
    @DisplayName("getAll should return roles list")
    void getAll_success() {
        when(roleRepositoryPort.findAll()).thenReturn(List.of(new Role(1L, RoleName.ADMIN)));
        when(roleServiceMapper.toDTO(any(Role.class))).thenReturn(new RoleDTO(1L, "ADMIN"));

        var result = roleService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("ADMIN");

        verify(roleRepositoryPort).findAll();
    }
}
