package com.renewsim.backend.role.dto;

import org.junit.jupiter.api.Test;

import com.renewsim.backend.role_service.dto.UpdateRolesRequestDTO;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UpdateRolesRequestDTOTest {

    @Test
    void testShouldCreateUpdateRolesRequestDTO() {
        UpdateRolesRequestDTO dto = new UpdateRolesRequestDTO(List.of("ADMIN", "USER"));

        assertNotNull(dto);
        assertEquals(2, dto.roles().size());
        assertTrue(dto.roles().contains("ADMIN"));
        assertTrue(dto.roles().contains("USER"));
    }

    @Test
    void testShouldSetAndGetRoles() {
        UpdateRolesRequestDTO dto = new UpdateRolesRequestDTO(List.of("USER"));

        assertEquals(1, dto.roles().size());
        assertEquals("USER", dto.roles().get(0));
    }

    @Test
    void testShouldHaveToStringRepresentation() {
        UpdateRolesRequestDTO dto = new UpdateRolesRequestDTO(List.of("ADMIN"));

        String toString = dto.toString();
        assertTrue(toString.contains("roles=[ADMIN]"));
    }
}
