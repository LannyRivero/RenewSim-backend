package com.renewsim.backend.technology_service.domain.model;

import com.renewsim.backend.technology_service.domain.exception.InvalidTechnologyParameterException;
import com.renewsim.backend.technology_service.domain.factory.TechnologyFactory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    void shouldDeactivateTechnologyPreservingBusinessFields() {
        Technology technology = new Technology(
                5L,
                "Wind Turbine",
                com.renewsim.backend.technology_service.domain.model.vo.EnergyType.WIND,
                new com.renewsim.backend.technology_service.domain.model.vo.Efficiency(35.0),
                new com.renewsim.backend.technology_service.domain.model.vo.InstallationCost(java.math.BigDecimal.valueOf(2000)),
                30,
                new com.renewsim.backend.technology_service.domain.model.vo.MaintenanceCost(java.math.BigDecimal.valueOf(120)),
                "Seeded description",
                true,
                java.time.Instant.parse("2026-05-10T08:00:00Z"),
                java.time.Instant.parse("2026-05-11T09:30:00Z"),
                new com.renewsim.backend.technology_service.domain.model.vo.EnvironmentalImpact(8.0),
                new com.renewsim.backend.technology_service.domain.model.vo.Co2Reduction(java.math.BigDecimal.valueOf(300)),
                new com.renewsim.backend.technology_service.domain.model.vo.CapacityFactor(35.0));

        Technology deactivated = technology.deactivate();

        assertThat(deactivated.isActive()).isFalse();
        assertThat(deactivated.getId()).isEqualTo(5L);
        assertThat(deactivated.getName()).isEqualTo("Wind Turbine");
        assertThat(deactivated.getDescription()).isEqualTo("Seeded description");
        assertThat(deactivated.getCreatedAt()).isEqualTo(java.time.Instant.parse("2026-05-10T08:00:00Z"));
    }
}
