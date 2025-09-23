package com.renewsim.backend.role_service.domain.policy;

import com.renewsim.backend.role_service.domain.model.RoleName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

class RolePolicyTest {

    @Test
    @DisplayName("normalizeRoleName should return ADMIN when input is lowercase 'admin'")
    void normalizeRoleName_lowercase_returnsAdmin() {
        RoleName result = RolePolicy.normalizeRoleName("admin");
        assertEquals(RoleName.ADMIN, result);
    }

    @Test
    @DisplayName("normalizeRoleName should return ADMIN when input is uppercase 'ADMIN'")
    void normalizeRoleName_uppercase_returnsAdmin() {
        RoleName result = RolePolicy.normalizeRoleName("ADMIN");
        assertEquals(RoleName.ADMIN, result);
    }

    @Test
    @DisplayName("normalizeRoleName should return ADMIN when input has mixed case 'AdMiN'")
    void normalizeRoleName_mixedCase_returnsAdmin() {
        RoleName result = RolePolicy.normalizeRoleName("AdMiN");
        assertEquals(RoleName.ADMIN, result);
    }

    @Test
    @DisplayName("normalizeRoleName should trim spaces and return ADMIN")
    void normalizeRoleName_withSpaces_returnsAdmin() {
        RoleName result = RolePolicy.normalizeRoleName("   admin   ");
        assertEquals(RoleName.ADMIN, result);
    }

    @Test
    @DisplayName("normalizeRoleName should throw IllegalArgumentException when input is null")
    void normalizeRoleName_nullInput_throwsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> RolePolicy.normalizeRoleName(null));
        assertEquals("Role name cannot be null or blank", exception.getMessage());
    }

    @Test
    @DisplayName("normalizeRoleName should throw IllegalArgumentException when role name is invalid")
    void normalizeRoleName_invalidInput_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> RolePolicy.normalizeRoleName("INVALID_ROLE"));
    }

    @Test
    @DisplayName("normalizeRoleName should throw IllegalArgumentException when input is empty string")
    void normalizeRoleName_emptyInput_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> RolePolicy.normalizeRoleName(""));
    }

    // ---------------------------
    // ensureNoDuplicateRoles
    // ---------------------------

    @Test
    @DisplayName("ensureNoDuplicateRoles should pass when roles are unique")
    void ensureNoDuplicateRoles_uniqueRoles_ok() {
        assertDoesNotThrow(() -> RolePolicy.ensureNoDuplicateRoles(Set.of(RoleName.USER, RoleName.ADMIN)));
    }

    @Test
    @DisplayName("ensureNoDuplicateRoles should throw when roles contain duplicates")
    void ensureNoDuplicateRoles_duplicateRoles_throwsException() {
        List<RoleName> roles = List.of(RoleName.USER, RoleName.USER); // List sí admite duplicados

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> RolePolicy.ensureNoDuplicateRoles(roles));

        assertEquals("Duplicate roles are not allowed", exception.getMessage());
    }

    @Test
    @DisplayName("ensureNoDuplicateRoles should pass when roles set is null")
    void ensureNoDuplicateRoles_nullSet_ok() {
        assertDoesNotThrow(() -> RolePolicy.ensureNoDuplicateRoles(null));
    }

    // ---------------------------
    // ensureAtLeastOneAdminRemaining
    // ---------------------------

    @Test
    @DisplayName("ensureAtLeastOneAdminRemaining should pass when removing ADMIN but other admins remain")
    void ensureAtLeastOneAdminRemaining_multipleAdmins_ok() {
        Set<RoleName> currentRoles = Set.of(RoleName.ADMIN, RoleName.USER);
        Set<RoleName> newRoles = Set.of(RoleName.USER); // admin removed
        long totalAdmins = 2;

        assertDoesNotThrow(() -> RolePolicy.ensureAtLeastOneAdminRemaining(currentRoles, newRoles, totalAdmins));
    }

    @Test
    @DisplayName("ensureAtLeastOneAdminRemaining should throw when removing the only ADMIN")
    void ensureAtLeastOneAdminRemaining_lastAdmin_throwsException() {
        Set<RoleName> currentRoles = Set.of(RoleName.ADMIN);
        Set<RoleName> newRoles = Set.of(); // admin removed
        long totalAdmins = 1;

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> RolePolicy.ensureAtLeastOneAdminRemaining(currentRoles, newRoles, totalAdmins));
        assertEquals("Cannot remove ADMIN role: at least one administrator must remain in the system",
                exception.getMessage());
    }

    @Test
    @DisplayName("ensureAtLeastOneAdminRemaining should pass when user keeps ADMIN role")
    void ensureAtLeastOneAdminRemaining_adminStillPresent_ok() {
        Set<RoleName> currentRoles = Set.of(RoleName.ADMIN, RoleName.USER);
        Set<RoleName> newRoles = Set.of(RoleName.ADMIN); // still admin
        long totalAdmins = 1;

        assertDoesNotThrow(() -> RolePolicy.ensureAtLeastOneAdminRemaining(currentRoles, newRoles, totalAdmins));
    }
}
