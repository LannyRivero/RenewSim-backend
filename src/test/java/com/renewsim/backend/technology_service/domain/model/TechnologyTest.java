package com.renewsim.backend.technology_service.domain.model;

import org.junit.jupiter.api.Test;

import com.renewsim.backend.technology_service.domain.exception.InvalidTechnologyParameterException;

import static org.junit.jupiter.api.Assertions.*;

class TechnologyTest {

    @Test
    void shouldCreateValidTechnology() {
        Technology tech = new Technology(
                "Solar Panel", 0.85, 1200, 100, 15, 250, 6000, "SOLAR");

        assertEquals("Solar Panel", tech.name());
        assertEquals(0.85, tech.efficiency());
    }

    @Test
    void shouldThrowWhenEfficiencyInvalid() {
        assertThrows(InvalidTechnologyParameterException.class,
                () -> new Technology("Invalid", 1.5, 1000, 100, 10, 200, 5000, "SOLAR"));
    }

    @Test
    void shouldCompareByNameIgnoringCase() {
        Technology t1 = new Technology("Solar", 0.8, 1000, 100, 10, 100, 5000, "SOLAR");
        Technology t2 = new Technology("solar", 0.9, 1200, 150, 12, 110, 5200, "SOLAR");

        assertEquals(t1, t2);
    }
}
