package com.renewsim.backend.technology_service.infraestructure.mapper;

import com.renewsim.backend.technology_service.application.result.*;
import com.renewsim.backend.technology_service.domain.model.Technology;
import com.renewsim.backend.technology_service.domain.model.vo.*;
import com.renewsim.backend.technology_service.infrastructure.mapper.TechnologyDtoMapper;
import com.renewsim.backend.technology_service.infrastructure.mapper.TechnologyDtoMapperImpl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link TechnologyDtoMapper}.
 * Verifies accurate and lossless mapping between domain models and DTO layers.
 */
class TechnologyDtoMapperTest extends BaseValueObjectTest {

    private final TechnologyDtoMapper mapper = new TechnologyDtoMapperImpl();

    private Technology sampleDomain() {
        return new Technology(
                "Solar Panel",
                EnergyType.SOLAR,
                new Efficiency(85.5),
                new InstallationCost(BigDecimal.valueOf(12000)),
                new MaintenanceCost(BigDecimal.valueOf(450)),
                new EnvironmentalImpact(5.8),
                new Co2Reduction(BigDecimal.valueOf(27.3)),
                new EnergyProduction(3200.5)
        );
    }

    @Test
    @DisplayName("Should map Technology domain → TechnologyQueryResultDTO correctly")
    void shouldMapToQueryResultDTO() {
        // ─────────────────────────────
        // ARRANGE
        // ─────────────────────────────
        Technology domain = sampleDomain();

        // ─────────────────────────────
        // ACT
        // ─────────────────────────────
        TechnologyQueryResultDTO dto = mapper.toQueryResult(domain);

        // ─────────────────────────────
        // ASSERT
        // ─────────────────────────────
        assertNotNull(dto);
        assertEquals(domain.getName(), dto.name());
        assertEquals(domain.getEnergyType().name(), dto.energyType());
        assertEquals(domain.getEfficiency().value(), dto.efficiency(), 0.001);
        assertEquals(domain.getInstallationCost().value().doubleValue(), dto.installationCost(), 0.001);
        assertEquals(domain.getMaintenanceCost().value().doubleValue(), dto.maintenanceCost(), 0.001);
        assertEquals(domain.getEnvironmentalImpact().value(), dto.environmentalImpact(), 0.001);
        assertEquals(domain.getCo2Reduction().value().doubleValue(), dto.co2Reduction(), 0.001);
        assertEquals(domain.getEnergyProduction().value(), dto.energyProduction(), 0.001);
    }

    @Test
    @DisplayName("Should map Technology domain → TechnologyCreationResultDTO correctly")
    void shouldMapToCreationResultDTO() {
        // ─────────────────────────────
        // ARRANGE
        // ─────────────────────────────
        Technology domain = sampleDomain();

        // ─────────────────────────────
        // ACT
        // ─────────────────────────────
        TechnologyCreationResultDTO dto = mapper.toCreationResult(domain);

        // ─────────────────────────────
        // ASSERT
        // ─────────────────────────────
        assertNotNull(dto);
        assertEquals(domain.getName(), dto.name());
        assertEquals(domain.getEnergyType().name(), dto.energyType());
        assertEquals(domain.getEfficiency().value(), dto.efficiency(), 0.001);
        assertEquals(domain.getInstallationCost().value().doubleValue(), dto.installationCost(), 0.001);
        assertEquals(domain.getMaintenanceCost().value().doubleValue(), dto.maintenanceCost(), 0.001);
        assertEquals(domain.getEnvironmentalImpact().value(), dto.environmentalImpact(), 0.001);
        assertEquals(domain.getCo2Reduction().value().doubleValue(), dto.co2Reduction(), 0.001);
        assertEquals(domain.getEnergyProduction().value(), dto.energyProduction(), 0.001);
    }

    @Test
    @DisplayName("Should map Technology domain → TechnologyUpdateResultDTO correctly")
    void shouldMapToUpdateResultDTO() {
        // ─────────────────────────────
        // ARRANGE
        // ─────────────────────────────
        Technology domain = sampleDomain();

        // ─────────────────────────────
        // ACT
        // ─────────────────────────────
        TechnologyUpdateResultDTO dto = mapper.toUpdateResult(domain);

        // ─────────────────────────────
        // ASSERT
        // ─────────────────────────────
        assertNotNull(dto);
        assertEquals(domain.getName(), dto.name());
        assertEquals(domain.getEnergyType().name(), dto.energyType());
        assertEquals(domain.getEfficiency().value(), dto.efficiency(), 0.001);
        assertEquals(domain.getInstallationCost().value().doubleValue(), dto.installationCost(), 0.001);
        assertEquals(domain.getMaintenanceCost().value().doubleValue(), dto.maintenanceCost(), 0.001);
        assertEquals(domain.getEnvironmentalImpact().value(), dto.environmentalImpact(), 0.001);
        assertEquals(domain.getCo2Reduction().value().doubleValue(), dto.co2Reduction(), 0.001);
        assertEquals(domain.getEnergyProduction().value(), dto.energyProduction(), 0.001);
    }
}
