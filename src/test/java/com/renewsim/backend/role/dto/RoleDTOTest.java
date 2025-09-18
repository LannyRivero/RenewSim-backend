package com.renewsim.backend.role.dto;

import org.junit.jupiter.api.Test;

import com.renewsim.backend.role_service.dto.RoleDTO;

import static org.junit.jupiter.api.Assertions.*;

class RoleDTOTest {

    @Test
    void testShouldCreateRoleDTOWithBuilder() {
        RoleDTO roleDTO = RoleDTO.builder()
                .id(1L)
                .name("ADMIN")
                .build();

        assertNotNull(roleDTO);
        assertEquals(1L, roleDTO.getId());
        assertEquals("ADMIN", roleDTO.getName());
    }

    @Test
    void testShouldSetAndGetFields() {
        RoleDTO roleDTO = new RoleDTO();
        roleDTO.setId(2L);
        roleDTO.setName("USER");

        assertEquals(2L, roleDTO.getId());
        assertEquals("USER", roleDTO.getName());
    }

    @Test
    void testShouldHaveToStringRepresentation() {
        RoleDTO roleDTO = RoleDTO.builder()
                .id(3L)
                .name("MODERATOR")
                .build();

        String toString = roleDTO.toString();
        assertTrue(toString.contains("id=3"));
        assertTrue(toString.contains("name=MODERATOR"));
    }
}
