package com.renewsim.backend.role_service.domain.policy;

import com.renewsim.backend.role_service.domain.model.RoleName;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> RolePolicy.normalizeRoleName(null));
        assertEquals(" Role name cannot be null", exception.getMessage());
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
}
