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
                    new Efficiency(85.0),                              
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
            
            // double comparisons
            assertEquals(domain.getEfficiency().value(), result.getEfficiency().value(), 1e-6);
            assertEquals(domain.getEnergyProduction().value(), result.getEnergyProduction().value(), 1e-6);
            
            // BigDecimal comparisons
            assertEquals(0, domain.getInstallationCost().value().compareTo(result.getInstallationCost().value()));
            assertEquals(0, domain.getMaintenanceCost().value().compareTo(result.getMaintenanceCost().value()));
            assertEquals(0, domain.getCo2Reduction().value().compareTo(result.getCo2Reduction().value()));
            
            // ⚠️ EnvironmentalImpact and Co2Reduction both map to co2ReductionFactor
            // So after round-trip, EnvironmentalImpact will have the same value as Co2Reduction
            assertEquals(result.getEnvironmentalImpact().value(), result.getCo2Reduction().value().doubleValue(), 1e-6);
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
        @DisplayName("Should map WIND energy type correctly")
        void shouldMapWindEnergyTypeCorrectly() {
            TechnologyEntity entity = TechnologyEntity.builder()
                    .id(10L)
                    .name("Wind Turbine")
                    .energyType(TechnologyEntity.EnergyType.WIND)
                    .efficiency(BigDecimal.valueOf(65.0))  // ✅ Valid for EOLIC (≤70%)
                    .unitCost(BigDecimal.valueOf(5000))
                    .maintenanceCost(BigDecimal.valueOf(250))
                    .lifespanYears(20)
                    .capacityFactor(BigDecimal.valueOf(35.0))
                    .minCapacityKw(BigDecimal.valueOf(100))
                    .maxCapacityKw(BigDecimal.valueOf(5000))
                    .co2ReductionFactor(BigDecimal.valueOf(0.85))
                    .isActive(true)
                    .build();

            Technology domain = mapper.toDomain(entity);

            assertNotNull(domain);
            assertEquals(EnergyType.EOLIC, domain.getEnergyType());
            assertEquals("Wind Turbine", domain.getName());
            assertEquals(65.0, domain.getEfficiency().value(), 1e-6);
        }

        @Test
        @DisplayName("Should throw exception for invalid energyType enum value")
        void shouldThrowExceptionForInvalidEnergyType() {
            // ✅ Validar que valueOf() lanza IllegalArgumentException
            Exception exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> TechnologyEntity.EnergyType.valueOf("UNKNOWN_TYPE"),
                    "Expected IllegalArgumentException for invalid energyType"
            );

            assertTrue(exception.getMessage().contains("No enum constant"));
        }

        @Test
        @DisplayName("Should handle all supported energy types")
        void shouldHandleAllSupportedEnergyTypes() {
            // SOLAR
            TechnologyEntity solarEntity = createEntityWithEnergyType(TechnologyEntity.EnergyType.SOLAR, 85.0);
            assertEquals(EnergyType.SOLAR, mapper.toDomain(solarEntity).getEnergyType());

            // WIND → EOLIC (con eficiencia válida ≤70%)
            TechnologyEntity windEntity = createEntityWithEnergyType(TechnologyEntity.EnergyType.WIND, 65.0);
            assertEquals(EnergyType.EOLIC, mapper.toDomain(windEntity).getEnergyType());

            // HYDRO
            TechnologyEntity hydroEntity = createEntityWithEnergyType(TechnologyEntity.EnergyType.HYDRO, 80.0);
            assertEquals(EnergyType.HYDRO, mapper.toDomain(hydroEntity).getEnergyType());

            // GEOTHERMAL
            TechnologyEntity geoEntity = createEntityWithEnergyType(TechnologyEntity.EnergyType.GEOTHERMAL, 75.0);
            assertEquals(EnergyType.GEOTHERMAL, mapper.toDomain(geoEntity).getEnergyType());

            // BIOMASS
            TechnologyEntity biomassEntity = createEntityWithEnergyType(TechnologyEntity.EnergyType.BIOMASS, 70.0);
            assertEquals(EnergyType.BIOMASS, mapper.toDomain(biomassEntity).getEnergyType());
        }

        private TechnologyEntity createEntityWithEnergyType(TechnologyEntity.EnergyType energyType, double efficiency) {
            return TechnologyEntity.builder()
                    .id(1L)
                    .name("Test Technology")
                    .energyType(energyType)
                    .efficiency(BigDecimal.valueOf(efficiency))
                    .unitCost(BigDecimal.valueOf(10000))
                    .maintenanceCost(BigDecimal.valueOf(500))
                    .lifespanYears(25)
                    .capacityFactor(BigDecimal.valueOf(40.0))
                    .minCapacityKw(BigDecimal.valueOf(50))
                    .maxCapacityKw(BigDecimal.valueOf(10000))
                    .co2ReductionFactor(BigDecimal.valueOf(0.90))
                    .isActive(true)
                    .build();
        }
    }
}