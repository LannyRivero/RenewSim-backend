package com.renewsim.backend.technology_service.domain.factory;

import com.renewsim.backend.technology_service.domain.exception.InvalidTechnologyParameterException;
import com.renewsim.backend.technology_service.domain.model.Technology;
import com.renewsim.backend.technology_service.domain.model.vo.EnergyType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Parameterized tests for TechnologyFactory.
 * Evaluates multiple combinations of input parameters for validation and
 * creation.
 */
class TechnologyFactoryParameterizedTest {

    // -----------------------------------------------------------
    // ✅ VALID SCENARIOS
    // -----------------------------------------------------------

    @ParameterizedTest(name = "Valid tech: {0} | Eff: {1}% | Type: {7}")
    @MethodSource("validTechnologyProvider")
    @DisplayName("Should create multiple valid technologies successfully")
    void shouldCreateMultipleValidTechnologies(String name, double efficiency, double installCost,
            double maintCost, double impact, double co2, double energy, String type) {
        Technology tech = TechnologyFactory.create(name, efficiency, installCost, maintCost, impact, co2, energy, type);
        assertNotNull(tech);
        assertEquals(name, tech.getName());
        assertEquals(EnergyType.fromString(type).name(), tech.getEnergyType().name());
        assertTrue(tech.getEfficiency().value() >= 0);
    }

    static Stream<Arguments> validTechnologyProvider() {
        return Stream.of(
                Arguments.of("Solar Basic", 85.0, 12000.0, 500.0, 5.0, 25.0, 18.0, "SOLAR"),
                Arguments.of("Wind Medium", 70.0, 15000.0, 800.0, 10.0, 35.0, 35.0, "EOLIC"),
                Arguments.of("Hydro High", 88.0, 1_200_000.0, 6000.0, 30.0, 90.0, 52.0, "HYDRO"),
                Arguments.of("Geo Basic", 75.0, 20000.0, 1000.0, 15.0, 40.0, 75.0, "GEOTHERMAL"));
    }

    // -----------------------------------------------------------
    // ❌ INVALID VALUE SCENARIOS (handled by VO)
    // -----------------------------------------------------------

    @ParameterizedTest(name = "Invalid value case → Eff: {1} | Install: {2} | Maint: {3}")
    @MethodSource("invalidValueProvider")
    @DisplayName("Should reject invalid primitive values via Value Objects")
    void shouldRejectInvalidValues(String name, double efficiency, double installCost,
            double maintCost, double impact, double co2, double energy, String type) {
        assertThrows(InvalidTechnologyParameterException.class,
                () -> TechnologyFactory.create(name, efficiency, installCost, maintCost, impact, co2, energy, type));
    }

    static Stream<Arguments> invalidValueProvider() {
        return Stream.of(
                // Efficiency invalid
                Arguments.of("BadEffHigh", 150.0, 10000.0, 400.0, 10.0, 20.0, 18.0, "SOLAR"),
                Arguments.of("BadEffLow", -10.0, 8000.0, 300.0, 5.0, 15.0, 18.0, "EOLIC"),
                // Cost invalid
                Arguments.of("NegativeCost", 80.0, -5000.0, 300.0, 10.0, 25.0, 35.0, "HYDRO"),
                // Name invalid
                Arguments.of("   ", 80.0, 8000.0, 200.0, 5.0, 10.0, 18.0, "GEOTHERMAL"));
    }

    // -----------------------------------------------------------
    // ❌ BUSINESS RULE SCENARIOS (TechnologyPolicy)
    // -----------------------------------------------------------

    @ParameterizedTest(name = "Policy violation → {0} | Eff: {1} | Type: {7}")
    @MethodSource("policyViolationProvider")
    @DisplayName("Should reject technologies that violate business rules")
    void shouldRejectBusinessPolicyViolations(String name, double efficiency, double installCost,
            double maintCost, double impact, double co2, double energy, String type) {
        assertThrows(InvalidTechnologyParameterException.class,
                () -> TechnologyFactory.create(name, efficiency, installCost, maintCost, impact, co2, energy, type));
    }

    static Stream<Arguments> policyViolationProvider() {
        return Stream.of(
                // Solar cannot exceed 95%
                Arguments.of("Solar Ultra", 99.0, 12000.0, 400.0, 10.0, 25.0, 18.0, "SOLAR"),
                // Maintenance > installation
                Arguments.of("GeoBad", 70.0, 4000.0, 6000.0, 15.0, 20.0, 35.0, "GEOTHERMAL"),
                // High-cost with high impact
                Arguments.of("HydroExpensive", 85.0, 1_500_000.0, 5000.0, 80.0, 40.0, 52.0, "HYDRO"));
    }

    // -----------------------------------------------------------
    // ✅ EDGE CASES
    // -----------------------------------------------------------

    @ParameterizedTest(name = "Edge case → Eff: {1} | Install: {2} | Maint: {3}")
    @CsvSource({
            "ZeroEff, 0.0, 1000.0, 100.0, 0.0, 0.0, 18.0, SOLAR",
            "LowImpact, 70.0, 5000.0, 100.0, 0.0, 10.0, 50.0, HYDRO",
            "HighEffLowCost, 65.0, 3000.0, 100.0, 20.0, 30.0, 30.0, EOLIC"
    })
    @DisplayName("Should handle edge cases gracefully")
    void shouldHandleEdgeCases(String name, double efficiency, double installCost,
            double maintCost, double impact, double co2, double energy, String type) {
        Technology tech = TechnologyFactory.create(name, efficiency, installCost, maintCost, impact, co2, energy, type);
        assertNotNull(tech);
        assertTrue(tech.getEfficiency().value() >= 0 && tech.getEfficiency().value() <= 100);
    }
}
