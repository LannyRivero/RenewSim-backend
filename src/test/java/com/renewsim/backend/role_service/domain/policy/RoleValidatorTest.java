package com.renewsim.backend.role_service.domain.policy;

import com.renewsim.backend.role_service.application.port.out.RoleRepositoryPort;
import com.renewsim.backend.role_service.domain.model.Role;
import com.renewsim.backend.role_service.domain.model.RoleName;
import com.renewsim.backend.shared.exception.LastAdminRemovalException;
import com.renewsim.backend.shared.exception.RoleAlreadyExistsException;
import com.renewsim.backend.shared.exception.RoleNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleValidatorTest {

    @Mock
    private RoleRepositoryPort roleRepositoryPort;

    @InjectMocks
    private RoleValidator roleValidator;

    @Test
    @DisplayName("validateRoleDoesNotExist should throw RoleAlreadyExistsException if role exists")
    void validateRoleDoesNotExist_roleExists_throwsException() {
        when(roleRepositoryPort.findByName(RoleName.ADMIN))
                .thenReturn(Optional.of(new Role(1L, RoleName.ADMIN)));

        RoleAlreadyExistsException exception = assertThrows(
                RoleAlreadyExistsException.class,
                () -> roleValidator.validateRoleDoesNotExist(RoleName.ADMIN));

        assertTrue(exception.getMessage().contains("Role already exists"));
        verify(roleRepositoryPort).findByName(RoleName.ADMIN);
    }

    @Test
    @DisplayName("validateRoleDoesNotExist should pass when role does not exist")
    void validateRoleDoesNotExist_roleNotExists_passes() {
        when(roleRepositoryPort.findByName(RoleName.USER)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> roleValidator.validateRoleDoesNotExist(RoleName.USER));
        verify(roleRepositoryPort).findByName(RoleName.USER);
    }

    @Test
    @DisplayName("validateRoleExists should throw RoleNotFoundException when role not found")
    void validateRoleExists_notFound_throwsException() {
        when(roleRepositoryPort.findById(99L)).thenReturn(Optional.empty());

        RoleNotFoundException exception = assertThrows(
                RoleNotFoundException.class,
                () -> roleValidator.validateRoleExists(99L));

        assertTrue(exception.getMessage().contains("Role with id=99 not found"));
        verify(roleRepositoryPort).findById(99L);
    }

    @Test
    @DisplayName("validateRoleExists should pass when role is found")
    void validateRoleExists_found_passes() {
        when(roleRepositoryPort.findById(1L))
                .thenReturn(Optional.of(new Role(1L, RoleName.ADMIN)));

        assertDoesNotThrow(() -> roleValidator.validateRoleExists(1L));
        verify(roleRepositoryPort).findById(1L);
    }

    @Test
    @DisplayName("validateNotRemovingLastAdmin should throw LastAdminRemovalException when trying to remove the last ADMIN")
    void validateNotRemovingLastAdmin_lastAdmin_throwsException() {
        long totalAdmins = 1;

        LastAdminRemovalException exception = assertThrows(
                LastAdminRemovalException.class,
                () -> roleValidator.validateNotRemovingLastAdmin(totalAdmins, RoleName.ADMIN));

        assertEquals("Cannot remove the last ADMIN role", exception.getMessage());
    }

    @Test
    @DisplayName("validateNotRemovingLastAdmin should pass when more than one ADMIN exists")
    void validateNotRemovingLastAdmin_multipleAdmins_passes() {
        assertDoesNotThrow(() -> roleValidator.validateNotRemovingLastAdmin(2, RoleName.ADMIN));
    }

    @Test
    @DisplayName("validateNotRemovingLastAdmin should pass when removing a non-ADMIN role")
    void validateNotRemovingLastAdmin_nonAdmin_passes() {
        assertDoesNotThrow(() -> roleValidator.validateNotRemovingLastAdmin(1, RoleName.USER));
    }

}
