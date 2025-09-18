package com.renewsim.backend.role;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.server.ResponseStatusException;

import com.renewsim.backend.role_service.domain.model.RoleName;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should return role when role exists")
    void testShouldReturnRoleWhenRoleExists() {
        Role role = new Role(RoleName.ADMIN);
        when(roleRepository.findByName(RoleName.ADMIN)).thenReturn(Optional.of(role));

        Role result = roleService.getRoleByName(RoleName.ADMIN);

        assertNotNull(result);
        assertEquals(RoleName.ADMIN, result.getName());
        verify(roleRepository).findByName(RoleName.ADMIN);
    }

    @Test
    @DisplayName("Should throw exception when role does not exist")
    void testShouldThrowExceptionWhenRoleDoesNotExist() {
        when(roleRepository.findByName(RoleName.ADMIN)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> roleService.getRoleByName(RoleName.ADMIN));

        assertTrue(exception.getMessage().contains("El rol ADMIN no existe"));
        verify(roleRepository).findByName(RoleName.ADMIN);
    }

    @Test
    @DisplayName("Should return roles from strings successfully")
    void testShouldReturnRolesFromStringsSuccessfully() {
        Role role = new Role(RoleName.USER);
        when(roleRepository.findByName(RoleName.USER)).thenReturn(Optional.of(role));

        Set<Role> roles = roleService.getRolesFromStrings(Set.of("USER"));

        assertNotNull(roles);
        assertEquals(1, roles.size());
        assertTrue(roles.contains(role));
    }

    @Test
    @DisplayName("Should throw exception for invalid role string")
    void testShouldThrowExceptionForInvalidRoleString() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> roleService.getRolesFromStrings(Set.of("INVALID_ROLE")));

        assertTrue(exception.getMessage().contains("Rol inválido"));
    }

    @Test
    @DisplayName("Should return roles by names successfully")
    void testShouldReturnRolesByNamesSuccessfully() {
        Role role = new Role(RoleName.USER);
        when(roleRepository.findByName(RoleName.USER)).thenReturn(Optional.of(role));

        Set<Role> roles = roleService.getRolesByNames(List.of("USER"));

        assertNotNull(roles);
        assertEquals(1, roles.size());
        assertTrue(roles.contains(role));
    }

    @Test
    @DisplayName("Should throw runtime exception when role not found by name")
    void testShouldThrowRuntimeExceptionWhenRoleNotFoundByName() {
        when(roleRepository.findByName(RoleName.USER)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> roleService.getRolesByNames(List.of("USER")));

        assertTrue(exception.getMessage().contains("Role not found: USER"));
    }
}
