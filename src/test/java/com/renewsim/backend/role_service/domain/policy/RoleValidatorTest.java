package com.renewsim.backend.role_service.domain.policy;

import com.renewsim.backend.role_service.application.port.out.RoleRepositoryPort;
import com.renewsim.backend.role_service.application.service.RoleValidator;
import com.renewsim.backend.role_service.domain.exception.LastAdminRemovalException;
import com.renewsim.backend.role_service.domain.model.Role;
import com.renewsim.backend.shared.domain.vo.RoleName;
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

    // ---------------------------
    // validateRoleDoesNotExist
    // ---------------------------

    @Test
    @DisplayName("validateRoleDoesNotExist should throw RoleAlreadyExistsException if role exists")
    void validateRoleDoesNotExist_exists_throwsException() {
        when(roleRepositoryPort.findByName(RoleName.ADMIN))
                .thenReturn(Optional.of(new Role(RoleName.ADMIN)));

        assertThrows(RoleAlreadyExistsException.class,
                () -> roleValidator.validateRoleDoesNotExist(RoleName.ADMIN));

        verify(roleRepositoryPort).findByName(RoleName.ADMIN);
    }

    @Test
    @DisplayName("validateRoleDoesNotExist should pass when role does not exist")
    void validateRoleDoesNotExist_notExists_ok() {
        when(roleRepositoryPort.findByName(RoleName.USER)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> roleValidator.validateRoleDoesNotExist(RoleName.USER));
        verify(roleRepositoryPort).findByName(RoleName.USER);
    }

    // ---------------------------
    // validateRoleExists
    // ---------------------------

    @Test
    @DisplayName("validateRoleExists should throw RoleNotFoundException when role not found")
    void validateRoleExists_notFound_throwsException() {
        when(roleRepositoryPort.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RoleNotFoundException.class,
                () -> roleValidator.validateRoleExists(99L));

        verify(roleRepositoryPort).findById(99L);
    }

    @Test
    @DisplayName("validateRoleExists should pass when role is found")
    void validateRoleExists_found_ok() {
        when(roleRepositoryPort.findById(1L))
                .thenReturn(Optional.of(new Role(RoleName.ADMIN)));

        assertDoesNotThrow(() -> roleValidator.validateRoleExists(1L));
        verify(roleRepositoryPort).findById(1L);
    }

    // ---------------------------
    // validateNotRemovingLastAdmin
    // ---------------------------

    @Test
    @DisplayName("validateNotRemovingLastAdmin should throw LastAdminRemovalException when removing last ADMIN")
    void validateNotRemovingLastAdmin_lastAdmin_throwsException() {
        assertThrows(LastAdminRemovalException.class,
                () -> roleValidator.validateNotRemovingLastAdmin(1, RoleName.ADMIN));
    }

    @Test
    @DisplayName("validateNotRemovingLastAdmin should pass when more than one ADMIN exists")
    void validateNotRemovingLastAdmin_multipleAdmins_ok() {
        assertDoesNotThrow(() -> roleValidator.validateNotRemovingLastAdmin(2, RoleName.ADMIN));
    }

    @Test
    @DisplayName("validateNotRemovingLastAdmin should pass when removing a non-ADMIN role")
    void validateNotRemovingLastAdmin_nonAdmin_ok() {
        assertDoesNotThrow(() -> roleValidator.validateNotRemovingLastAdmin(1, RoleName.USER));
    }
}
