package com.renewsim.backend.role;

import org.junit.jupiter.api.Test;

import com.renewsim.backend.role_service.domain.model.RoleName;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;

class RoleTest {

    @Test
    @DisplayName("Should create role with name")
    void testShouldCreateRoleWithName() {
        Role role = new Role(RoleName.ADMIN);

        assertNotNull(role);
        assertEquals(RoleName.ADMIN, role.getName());
    }

    @Test
    @DisplayName("Should set and get id and name")
    void testShouldSetAndGetIdAndName() {
        Role role = new Role();
        role.setId(1L);
        role.setName(RoleName.USER);

        assertEquals(1L, role.getId());
        assertEquals(RoleName.USER, role.getName());
    }

    @Test
    @DisplayName("Should verify equals and hashCode")
    void testShouldVerifyEqualsAndHashCode() {
        Role role1 = new Role(RoleName.ADMIN);
        Role role2 = new Role(RoleName.ADMIN);
        Role role3 = new Role(RoleName.USER);

        assertEquals(role1, role2);
        assertEquals(role1.hashCode(), role2.hashCode());

        assertNotEquals(role1, role3);
        assertNotEquals(role1.hashCode(), role3.hashCode());
    }

    @Test
    @DisplayName("Should have toString representation")
    void testShouldHaveToStringRepresentation() {
        Role role = new Role(RoleName.USER);
        String toString = role.toString();

        assertTrue(toString.contains("Role{name=USER}"));
    }

    @Test
    @DisplayName("Should return false when comparing Role with different object type")
    void testShouldReturnFalseWhenComparingToDifferentType() {
        Role role = new Role(RoleName.USER);
        assertNotEquals(role, "not a role");
    }

}
