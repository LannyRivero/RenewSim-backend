package com.renewsim.backend.technology_service.application.result;

import org.junit.Test;
import static org.junit.Assert.*;

public class TechnologyDeletionResultDTOTest {

    @Test
    public void testTechnologyDeletionResultDTO() {
        Long id = 1L;
        boolean success = true;
        String message = "Technology deleted successfully";
        TechnologyDeletionResultDTO dto = new TechnologyDeletionResultDTO(id, success, message);

        assertEquals(id, dto.id());
        assertEquals(success, dto.success());
        assertEquals(message, dto.message());
    }

    @Test
    public void testEquality() {
        TechnologyDeletionResultDTO dto1 = new TechnologyDeletionResultDTO(1L, true, "Success");
        TechnologyDeletionResultDTO dto2 = new TechnologyDeletionResultDTO(1L, true, "Success");
        TechnologyDeletionResultDTO dto3 = new TechnologyDeletionResultDTO(2L, false, "Failure");

        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
    }

    @Test
    public void testHashCode() {
        TechnologyDeletionResultDTO dto1 = new TechnologyDeletionResultDTO(1L, true, "Success");
        TechnologyDeletionResultDTO dto2 = new TechnologyDeletionResultDTO(1L, true, "Success");

        assertEquals(dto1.hashCode(), dto2.hashCode());
    }
}