package com.renewsim.backend.role_service.domain.policy;

import com.renewsim.backend.role_service.domain.exception.DuplicateRoleException;
import com.renewsim.backend.role_service.domain.exception.InvalidRoleNameException;
import com.renewsim.backend.role_service.domain.exception.LastAdminRemovalException;
import com.renewsim.backend.role_service.domain.exception.UnauthorizedRoleAssignmentException;
import com.renewsim.backend.shared.domain.vo.RoleName;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RolePolicyTest {

    // ---------------------------
    // normalizeRoleName
    // ---------------------------

    @Test
    @DisplayName("normalizeRoleName should return ADMIN when input is lowercase 'admin'")
    void normalizeRoleName_lowercase_returnsAdmin() {
        RoleName result = RolePolicy.normalizeRoleName("admin");
        assertEquals(RoleName.ADMIN, result);
    }

    @Test
    @DisplayName("normalizeRoleName should return ADMIN when input has spaces")
    void normalizeRoleName_withSpaces_returnsAdmin() {
        RoleName result = RolePolicy.normalizeRoleName("   admin   ");
        assertEquals(RoleName.ADMIN, result);
    }

    @Test
    @DisplayName("normalizeRoleName should throw InvalidRoleNameException when input is null")
    void normalizeRoleName_nullInput_throwsException() {
        InvalidRoleNameException ex = assertThrows(
                InvalidRoleNameException.class,
                () -> RolePolicy.normalizeRoleName(null));
        assertEquals("Role name cannot be null or blank", ex.getMessage());
    }

    @Test
    @DisplayName("normalizeRoleName should throw InvalidRoleNameException when input is blank")
    void normalizeRoleName_blankInput_throwsException() {
        assertThrows(InvalidRoleNameException.class, () -> RolePolicy.normalizeRoleName("   "));
    }

    @Test
    @DisplayName("normalizeRoleName should throw IllegalArgumentException when role does not exist in enum")
    void normalizeRoleName_invalidRole_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> RolePolicy.normalizeRoleName("INVALID"));
    }

    // ---------------------------
    // ensureNoDuplicateRoles
    // ---------------------------

    @Test
    @DisplayName("ensureNoDuplicateRoles should pass when roles are unique")
    void ensureNoDuplicateRoles_unique_ok() {
        assertDoesNotThrow(() -> RolePolicy.ensureNoDuplicateRoles(Set.of(RoleName.USER, RoleName.ADMIN)));
    }

    @Test
    @DisplayName("ensureNoDuplicateRoles should throw DuplicateRoleException when duplicates exist")
    void ensureNoDuplicateRoles_duplicates_throwsException() {
        List<RoleName> roles = List.of(RoleName.USER, RoleName.USER);

        DuplicateRoleException ex = assertThrows(
                DuplicateRoleException.class,
                () -> RolePolicy.ensureNoDuplicateRoles(roles));

        assertEquals("Duplicate roles are not allowed", ex.getMessage());
    }

    @Test
    @DisplayName("ensureNoDuplicateRoles should pass when roles is null")
    void ensureNoDuplicateRoles_null_ok() {
        assertDoesNotThrow(() -> RolePolicy.ensureNoDuplicateRoles(null));
    }

    // ---------------------------
    // ensureUserCannotAssignAdmin
    // ---------------------------

    @Test
    @DisplayName("ensureUserCannotAssignAdmin should throw UnauthorizedRoleAssignmentException when USER assigns ADMIN")
    void ensureUserCannotAssignAdmin_userAssignsAdmin_throwsException() {
        UnauthorizedRoleAssignmentException ex = assertThrows(
                UnauthorizedRoleAssignmentException.class,
                () -> RolePolicy.ensureUserCannotAssignAdmin(RoleName.USER, RoleName.ADMIN));
        assertEquals("USER cannot assign ADMIN role", ex.getMessage());
    }

    @Test
    @DisplayName("ensureUserCannotAssignAdmin should pass when ADMIN assigns ADMIN")
    void ensureUserCannotAssignAdmin_adminAssignsAdmin_ok() {
        assertDoesNotThrow(() -> RolePolicy.ensureUserCannotAssignAdmin(RoleName.ADMIN, RoleName.ADMIN));
    }

    // ---------------------------
    // ensureAtLeastOneAdminRemaining
    // ---------------------------

    @Test
    @DisplayName("ensureAtLeastOneAdminRemaining should pass when removing ADMIN but others remain")
    void ensureAtLeastOneAdminRemaining_multipleAdmins_ok() {
        Set<RoleName> currentRoles = Set.of(RoleName.ADMIN, RoleName.USER);
        Set<RoleName> newRoles = Set.of(RoleName.USER); // admin removed
        long totalAdmins = 2;

        assertDoesNotThrow(() -> RolePolicy.ensureAtLeastOneAdminRemaining(currentRoles, newRoles, totalAdmins));
    }

    @Test
    @DisplayName("ensureAtLeastOneAdminRemaining should throw LastAdminRemovalException when removing the only ADMIN")
    void ensureAtLeastOneAdminRemaining_lastAdmin_throwsException() {
        Set<RoleName> currentRoles = Set.of(RoleName.ADMIN);
        Set<RoleName> newRoles = Set.of(); // admin removed
        long totalAdmins = 1;

        LastAdminRemovalException ex = assertThrows(
                LastAdminRemovalException.class,
                () -> RolePolicy.ensureAtLeastOneAdminRemaining(currentRoles, newRoles, totalAdmins));

        assertEquals(
                "Cannot remove ADMIN role: at least one administrator must remain in the system",
                ex.getMessage());
    }

    @Test
    @DisplayName("ensureAtLeastOneAdminRemaining should pass when ADMIN is still present")
    void ensureAtLeastOneAdminRemaining_adminStillPresent_ok() {
        Set<RoleName> currentRoles = Set.of(RoleName.ADMIN, RoleName.USER);
        Set<RoleName> newRoles = Set.of(RoleName.ADMIN);
        long totalAdmins = 1;

        assertDoesNotThrow(() -> RolePolicy.ensureAtLeastOneAdminRemaining(currentRoles, newRoles, totalAdmins));
    }
}
