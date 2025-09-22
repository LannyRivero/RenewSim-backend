package com.renewsim.backend.role_service.application.service;

import com.renewsim.backend.role_service.application.port.out.RoleRepositoryPort;
import com.renewsim.backend.role_service.domain.model.Role;
import com.renewsim.backend.role_service.domain.model.RoleName;
import com.renewsim.backend.role_service.dto.RoleDTO;
import com.renewsim.backend.shared.exception.RoleAlreadyExistsException;
import com.renewsim.backend.shared.exception.RoleNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoleServiceImplTest {

    private RoleRepositoryPort roleRepositoryPort;
    private RoleServiceImpl roleService;

    @BeforeEach
    void setUp() {
        roleRepositoryPort = mock(RoleRepositoryPort.class);
        roleService = new RoleServiceImpl(roleRepositoryPort);
    }

    @Test
    @DisplayName("create should save role when not exists")
    void create_success() {
        RoleDTO dto = new RoleDTO(null, "ADMIN");
        Role role = new Role(1L, RoleName.ADMIN);

        when(roleRepositoryPort.findByName(RoleName.ADMIN)).thenReturn(Optional.empty());
        when(roleRepositoryPort.save(any())).thenReturn(role);

        RoleDTO result = roleService.create(dto);

        assertThat(result.name()).isEqualTo("ADMIN");
        verify(roleRepositoryPort).save(any());
    }

    @Test
    @DisplayName("create should throw when role already exists")
    void create_conflict() {
        when(roleRepositoryPort.findByName(RoleName.ADMIN)).thenReturn(Optional.of(new Role(1L, RoleName.ADMIN)));

        RoleDTO dto = new RoleDTO(null, "ADMIN");

        assertThatThrownBy(() -> roleService.create(dto))
                .isInstanceOf(RoleAlreadyExistsException.class)
                .hasMessageContaining("Role already exists");
    }

    @Test
    @DisplayName("delete should remove role when exists")
    void delete_success() {
        when(roleRepositoryPort.findById(1L)).thenReturn(Optional.of(new Role(1L, RoleName.USER)));

        roleService.delete(1L);

        verify(roleRepositoryPort).deleteById(1L);
    }

    @Test
    @DisplayName("delete should throw when role not found")
    void delete_notFound() {
        when(roleRepositoryPort.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> roleService.delete(99L))
                .isInstanceOf(RoleNotFoundException.class);
    }

    @Test
    @DisplayName("getAll should return roles list")
    void getAll_success() {
        when(roleRepositoryPort.findAll()).thenReturn(List.of(new Role(1L, RoleName.ADMIN)));

        List<RoleDTO> result = roleService.getAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("ADMIN");
    }
}
