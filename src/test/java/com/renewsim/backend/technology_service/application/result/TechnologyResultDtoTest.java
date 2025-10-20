package com.renewsim.backend.technology_service.application.result;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Sanity tests for ResultDTO records (immutability, equality, and data
 * consistency).
 */
class TechnologyResultDtoTest {

    @Test
    @DisplayName("Should correctly create and compare TechnologyCreationResultDTO")
    void shouldCreateAndCompareCreationResult() {
        var dto1 = new TechnologyCreationResultDTO(
                1L, "Solar", "SOLAR", 0.85, 12000, 400, 10, 25, 3000, true, "Created");
        var dto2 = new TechnologyCreationResultDTO(
                1L, "Solar", "SOLAR", 0.85, 12000, 400, 10, 25, 3000, true, "Created");

        assertEquals(dto1, dto2);
        assertEquals("SOLAR", dto1.energyType());
        assertTrue(dto1.success());
        assertTrue(dto1.toString().contains("Solar"));
    }

    @Test
    @DisplayName("Should store and read values correctly in TechnologyUpdateResultDTO")
    void shouldReadUpdateResult() {
        var result = new TechnologyUpdateResultDTO(
                1L, "Wind", "EOLIC", 0.75, 20000, 600, 15, 40, 5000, true, "Updated");
        assertEquals("Updated", result.message());
        assertEquals(0.75, result.efficiency());
        assertEquals(20000, result.installationCost());
    }
}
