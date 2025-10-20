package com.renewsim.backend.technology_service.domain.policy;

import com.renewsim.backend.technology_service.domain.exception.InvalidTechnologyParameterException;
import com.renewsim.backend.technology_service.domain.model.Technology;
import com.renewsim.backend.technology_service.domain.model.vo.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TechnologyPolicy.validateCompatibility().
 * Ensures all domain-level business rules behave as expected.
 */
@Tag("unit")
class TechnologyPolicyTest {

    private Technology baseTechnology() {
        return new Technology(
                "Base Tech",
                EnergyType.SOLAR,
                new Efficiency(80.0),
                new InstallationCost(BigDecimal.valueOf(10000)),
                new MaintenanceCost(BigDecimal.valueOf(500)),
                new EnvironmentalImpact(20.0),
                new Co2Reduction(BigDecimal.valueOf(50.0)),
                new EnergyProduction(2000.0)
        );
    }

    @Test
    @DisplayName("✅ Should accept valid technology configuration")
    void shouldAcceptValidTechnology() {
        assertDoesNotThrow(this::baseTechnology);
    }

    @Test
    @DisplayName("❌ Should reject solar technologies exceeding 95% efficiency")
    void shouldRejectSolarWithUnrealisticEfficiency() {
        InvalidTechnologyParameterException ex = assertThrows(
                InvalidTechnologyParameterException.class,
                () -> new Technology(
                        "Solar Ultra",
                        EnergyType.SOLAR,
                        new Efficiency(99.0),
                        new InstallationCost(BigDecimal.valueOf(12000)),
                        new MaintenanceCost(BigDecimal.valueOf(400)),
                        new EnvironmentalImpact(10.0),
                        new Co2Reduction(BigDecimal.valueOf(25.0)),
                        new EnergyProduction(2000.0)
                ));
        assertTrue(ex.getMessage().contains("Solar technologies cannot exceed"));
    }

    @Test
    @DisplayName("❌ Should reject eolic technologies exceeding 70% efficiency")
    void shouldRejectEolicWithHighEfficiency() {
        InvalidTechnologyParameterException ex = assertThrows(
                InvalidTechnologyParameterException.class,
                () -> new Technology(
                        "WindMaster",
                        EnergyType.EOLIC,
                        new Efficiency(80.0),
                        new InstallationCost(BigDecimal.valueOf(25000)),
                        new MaintenanceCost(BigDecimal.valueOf(500)),
                        new EnvironmentalImpact(15.0),
                        new Co2Reduction(BigDecimal.valueOf(40.0)),
                        new EnergyProduction(3000.0)
                ));
        assertTrue(ex.getMessage().contains("Eolic technologies cannot exceed"));
    }

    @Test
    @DisplayName("❌ Should reject high-cost technologies with high environmental impact")
    void shouldRejectHighCostWithHighImpact() {
        InvalidTechnologyParameterException ex = assertThrows(
                InvalidTechnologyParameterException.class,
                () -> new Technology(
                        "HydroMax",
                        EnergyType.HYDRO,
                        new Efficiency(85.0),
                        new InstallationCost(BigDecimal.valueOf(1_500_000)),
                        new MaintenanceCost(BigDecimal.valueOf(8000)),
                        new EnvironmentalImpact(80.0),
                        new Co2Reduction(BigDecimal.valueOf(90.0)),
                        new EnergyProduction(4000.0)
                ));
        assertTrue(ex.getMessage().contains("High-cost technologies"));
    }

    @Test
    @DisplayName("❌ Should reject low-production technologies with unrealistic CO₂ reduction")
    void shouldRejectInconsistentCo2Reduction() {
        InvalidTechnologyParameterException ex = assertThrows(
                InvalidTechnologyParameterException.class,
                () -> new Technology(
                        "BioFlux",
                        EnergyType.BIOMASS,
                        new Efficiency(60.0),
                        new InstallationCost(BigDecimal.valueOf(30000)),
                        new MaintenanceCost(BigDecimal.valueOf(1000)),
                        new EnvironmentalImpact(25.0),
                        new Co2Reduction(BigDecimal.valueOf(200.0)),
                        new EnergyProduction(50.0)
                ));
        assertTrue(ex.getMessage().contains("Low-production technologies"));
    }

    @Test
    @DisplayName("❌ Should reject maintenance cost higher than installation cost")
    void shouldRejectMaintenanceHigherThanInstallation() {
        InvalidTechnologyParameterException ex = assertThrows(
                InvalidTechnologyParameterException.class,
                () -> new Technology(
                        "GeoTech",
                        EnergyType.GEOTHERMAL,
                        new Efficiency(75.0),
                        new InstallationCost(BigDecimal.valueOf(5000)),
                        new MaintenanceCost(BigDecimal.valueOf(6000)),
                        new EnvironmentalImpact(15.0),
                        new Co2Reduction(BigDecimal.valueOf(10.0)),
                        new EnergyProduction(1000.0)
                ));
        assertTrue(ex.getMessage().contains("Maintenance cost"));
    }

    @Test
    @DisplayName("✅ Should accept high-cost technologies with low environmental impact")
    void shouldAcceptHighCostWithLowImpact() {
        assertDoesNotThrow(() -> new Technology(
                "HydroClean",
                EnergyType.HYDRO,
                new Efficiency(88.0),
                new InstallationCost(BigDecimal.valueOf(1_200_000)),
                new MaintenanceCost(BigDecimal.valueOf(6000)),
                new EnvironmentalImpact(30.0),
                new Co2Reduction(BigDecimal.valueOf(100.0)),
                new EnergyProduction(8000.0)
        ));
    }

    @Test
    @DisplayName("✅ Should accept eolic technology within efficiency limit")
    void shouldAcceptValidEolicEfficiency() {
        assertDoesNotThrow(() -> new Technology(
                "WindSafe",
                EnergyType.EOLIC,
                new Efficiency(65.0),
                new InstallationCost(BigDecimal.valueOf(50000)),
                new MaintenanceCost(BigDecimal.valueOf(1000)),
                new EnvironmentalImpact(20.0),
                new Co2Reduction(BigDecimal.valueOf(60.0)),
                new EnergyProduction(3000.0)
        ));
    }
}
