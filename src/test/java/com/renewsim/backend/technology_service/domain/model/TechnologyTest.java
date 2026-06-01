package com.renewsim.backend.technology_service.domain.model;

import com.renewsim.backend.technology_service.domain.exception.InvalidTechnologyParameterException;
import com.renewsim.backend.technology_service.domain.factory.TechnologyFactory;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TechnologyTest {

    @Test
    void shouldCreateValidTechnology() {
        Technology tech = TechnologyFactory.create(
                "Solar Panel", 85.0, 1200, 100, 15, 250, 18.0, "SOLAR"
        );

        assertEquals("Solar Panel", tech.getName());
        assertEquals(85.0, tech.getEfficiency().value());
        assertEquals("SOLAR", tech.getEnergyType().name());
    }

    @Test
    void shouldThrowWhenEfficiencyInvalid() {
        assertThrows(InvalidTechnologyParameterException.class, () ->
                TechnologyFactory.create("Invalid", 150.0, 1000, 100, 10, 200, 18.0, "SOLAR"));
    }

    @Test
    void shouldCompareByNameIgnoringCase() {
        Technology t1 = TechnologyFactory.create("Solar", 80.0, 1000, 100, 10, 100, 18.0, "SOLAR");
        Technology t2 = TechnologyFactory.create("solar", 90.0, 1200, 150, 12, 110, 35.0, "SOLAR");

        assertEquals(t1, t2);
    }
}
