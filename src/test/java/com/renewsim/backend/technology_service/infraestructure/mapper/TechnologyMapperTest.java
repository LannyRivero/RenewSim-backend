package com.renewsim.backend.technology_service.infraestructure.mapper;

import com.renewsim.backend.technology_service.domain.model.Technology;
import com.renewsim.backend.technology_service.domain.model.vo.*;
import com.renewsim.backend.technology_service.infrastructure.mapper.TechnologyMapper;
import com.renewsim.backend.technology_service.infrastructure.mapper.TechnologyMapperImpl;
import com.renewsim.backend.technology_service.infrastructure.persistence.entity.TechnologyEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ✅ Unit test for {@link TechnologyMapper}
 *
 * Ensures accurate and safe bidirectional conversion between Domain and Entity layers.
 * Covers both happy-path and edge cases to validate mapper robustness.
 */
class TechnologyMapperTest {

    private final TechnologyMapper mapper = new TechnologyMapperImpl();

    // ============================================================
    // 🧩 HAPPY PATH
    // ============================================================
    @Nested
    @DisplayName("✅ Happy Path Mappings")
    class HappyPath {

        @Test
        @DisplayName("Should map Domain → Entity and back preserving all values")
        void shouldMapDomainToEntityAndBack() {
            // ARRANGE
            Technology domain = new Technology(
                    1L,
                    "Solar Panel",
                    EnergyType.SOLAR,
                    new Efficiency(0.85),
                    new InstallationCost(BigDecimal.valueOf(12000)),
                    new MaintenanceCost(BigDecimal.valueOf(400)),
                    new EnvironmentalImpact(5.0),
                    new Co2Reduction(BigDecimal.valueOf(25.0)),
                    new EnergyProduction(3000.0)
            );

            // ACT
            TechnologyEntity entity = mapper.toEntity(domain);
            Technology result = mapper.toDomain(entity);

            // ASSERT
            assertNotNull(entity, "Entity must not be null");
            assertNotNull(result, "Mapped domain must not be null");

            // Compare field by field
            assertEquals(domain.getName(), result.getName());
            assertEquals(domain.getEnergyType(), result.getEnergyType());
            assertEquals(domain.getEfficiency().value(), result.getEfficiency().value(), 1e-6);
            assertEquals(domain.getInstallationCost().value().doubleValue(), result.getInstallationCost().value().doubleValue(), 1e-6);
            assertEquals(domain.getMaintenanceCost().value().doubleValue(), result.getMaintenanceCost().value().doubleValue(), 1e-6);
            assertEquals(domain.getEnvironmentalImpact().value(), result.getEnvironmentalImpact().value(), 1e-6);
            assertEquals(domain.getCo2Reduction().value().doubleValue(), result.getCo2Reduction().value().doubleValue(), 1e-6);
            assertEquals(domain.getEnergyProduction().value(), result.getEnergyProduction().value(), 1e-6);
        }
    }

    // ============================================================
    // ⚠️ EDGE CASES
    // ============================================================
    @Nested
    @DisplayName("⚠️ Edge Cases & Error Handling")
    class EdgeCases {

        @Test
        @DisplayName("Should return null when mapping null entity")
        void shouldReturnNullWhenEntityIsNull() {
            assertNull(mapper.toDomain(null), "Mapping a null entity should return null");
        }

        @Test
        @DisplayName("Should handle lowercase energyType gracefully")
        void shouldHandleLowercaseEnergyType() {
            TechnologyEntity entity = TechnologyEntity.builder()
                    .id(10L)
                    .name("Wind Turbine")
                    .energyType("eolic") // lowercase
                    .efficiency(0.75)
                    .installationCost(5000)
                    .maintenanceCost(250)
                    .environmentalImpact(1.5)
                    .co2Reduction(200)
                    .energyProduction(4500)
                    .build();

            Technology domain = mapper.toDomain(entity);

            assertEquals(EnergyType.EOLIC, domain.getEnergyType());
        }

        @Test
        @DisplayName("Should throw exception for invalid energyType value")
        void shouldThrowExceptionForInvalidEnergyType() {
            TechnologyEntity invalidEntity = TechnologyEntity.builder()
                    .id(5L)
                    .name("Invalid Tech")
                    .energyType("UNKNOWN_TYPE")
                    .efficiency(0.5)
                    .installationCost(1000)
                    .maintenanceCost(200)
                    .environmentalImpact(0.3)
                    .co2Reduction(10)
                    .energyProduction(100)
                    .build();

            Exception exception = assertThrows(
                    IllegalStateException.class,
                    () -> mapper.toDomain(invalidEntity),
                    "Expected an exception for invalid energyType"
            );

            assertTrue(exception.getMessage().contains("Unknown energy type"));
        }
    }
}
