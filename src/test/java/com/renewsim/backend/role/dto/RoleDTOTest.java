package com.renewsim.backend.role.dto;

import org.junit.jupiter.api.Test;

import com.renewsim.backend.role_service.dto.RoleDTO;

import static org.junit.jupiter.api.Assertions.*;

class RoleDTOTest {

    @Test
    void testShouldCreateRoleDTOWithBuilder() {
        RoleDTO roleDTO = new RoleDTO(1L, "ADMIN");

        assertNotNull(roleDTO);
        assertEquals(1L, roleDTO.id());
        assertEquals("ADMIN", roleDTO.name());
    }

    @Test
    void testShouldSetAndGetFields() {
        RoleDTO roleDTO = new RoleDTO(2L, "USER");

        assertEquals(2L, roleDTO.id());
        assertEquals("USER", roleDTO.name());
    }

    @Test
    void testShouldHaveToStringRepresentation() {
        RoleDTO roleDTO = new RoleDTO(3L, "MODERATOR");

        String toString = roleDTO.toString();
        assertTrue(toString.contains("id=3"));
        assertTrue(toString.contains("name=MODERATOR"));
    }
 }