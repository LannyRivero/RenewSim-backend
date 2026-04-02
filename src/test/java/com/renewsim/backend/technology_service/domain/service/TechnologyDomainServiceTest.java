package com.renewsim.backend.technology_service.domain.service;

import com.renewsim.backend.technology_service.domain.model.Technology;
import com.renewsim.backend.technology_service.domain.model.vo.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TechnologyDomainService.
 * Validates all domain-level calculations and selection logic.
 */
class TechnologyDomainServiceTest {

    private final TechnologyDomainService service = new TechnologyDomainService();

    private Technology solarTech() {
        return new Technology(
                "Solar Panel",
                EnergyType.SOLAR,
                new Efficiency(85.0),
                new InstallationCost(BigDecimal.valueOf(10000)),
                new MaintenanceCost(BigDecimal.valueOf(500)),
                new EnvironmentalImpact(10.0),
                new Co2Reduction(20.0),
                new EnergyProduction(3000.0));
    }

    private Technology windTech() {
        return new Technology(
                "Wind Turbine",
                EnergyType.EOLIC,
                new Efficiency(70.0),
                new InstallationCost(BigDecimal.valueOf(15000)),
                new MaintenanceCost(BigDecimal.valueOf(800)),
                new EnvironmentalImpact(15.0),
                new Co2Reduction(30.0),
                new EnergyProduction(5000.0));
    }

    private Technology hydroTech() {
        return new Technology(
                "Hydro Plant",
                EnergyType.HYDRO,
                new Efficiency(90.0),
                new InstallationCost(BigDecimal.valueOf(30000)),
                new MaintenanceCost(BigDecimal.valueOf(1000)),
                new EnvironmentalImpact(5.0),
                new Co2Reduction(50.0),
                new EnergyProduction(8000.0));
    }

    // ----------------------------------------------------------
    // NORMALIZATION
    // ----------------------------------------------------------

    @Test
    @DisplayName("Should normalize values within range [0,1]")
    void shouldNormalizeWithinRange() {
        double normalized = service.normalize(50, 0, 100);
        assertEquals(0.5, normalized, 0.0001);
    }

    @Test
    @DisplayName("Should return 0 when max equals min to avoid division by zero")
    void shouldReturnZeroWhenMaxEqualsMin() {
        double result = service.normalize(50, 10, 10);
        assertEquals(0.0, result);
    }

    @Test
    @DisplayName("Should throw exception when inputs contain NaN")
    void shouldThrowOnNaNInputs() {
        assertThrows(IllegalArgumentException.class, () -> service.normalize(Double.NaN, 0, 100));
    }

    // ----------------------------------------------------------
    // SCORE CALCULATION
    // ----------------------------------------------------------

    @Test
    @DisplayName("Should calculate score prioritizing efficiency and low impact")
    void shouldCalculateValidScore() {
        double score = service.calculateScore(solarTech());
        assertTrue(score > 0);
    }

    @Test
    @DisplayName("Should throw exception if technology is null when calculating score")
    void shouldThrowWhenCalculatingScoreWithNullTech() {
        assertThrows(IllegalArgumentException.class, () -> service.calculateScore(null));
    }

    // ----------------------------------------------------------
    // FIND MOST EFFICIENT
    // ----------------------------------------------------------

    @Test
    @DisplayName("Should find technology with best efficiency-to-cost ratio")
    void shouldFindMostEfficientTechnology() {
        List<Technology> technologies = List.of(solarTech(), windTech(), hydroTech());
        Optional<Technology> result = service.findMostEfficient(technologies);

        assertTrue(result.isPresent());
        assertEquals("Solar Panel", result.get().getName());
    }

    @Test
    @DisplayName("Should return empty Optional when list is null or empty")
    void shouldReturnEmptyWhenListIsInvalid() {
        assertTrue(service.findMostEfficient(null).isEmpty());
        assertTrue(service.findMostEfficient(List.of()).isEmpty());
    }

    // ----------------------------------------------------------
    // FIND BEST OVERALL
    // ----------------------------------------------------------

    @Test
    @DisplayName("Should return technology with highest overall score")
    void shouldFindBestOverallTechnology() {
        List<Technology> technologies = List.of(solarTech(), windTech(), hydroTech());
        Optional<Technology> result = service.findBestOverall(technologies);

        assertTrue(result.isPresent());
        assertEquals("Hydro Plant", result.get().getName());
    }

    @Test
    @DisplayName("Should return empty Optional when list is empty in best overall")
    void shouldReturnEmptyInFindBestOverallWhenListEmpty() {
        assertTrue(service.findBestOverall(List.of()).isEmpty());
    }

    // ----------------------------------------------------------
    // EDGE CASES
    // ----------------------------------------------------------

    @Test
    @DisplayName("Should handle cost ratio when total cost is zero safely")
    void shouldHandleZeroTotalCostGracefully() {
        Technology zeroCostTech = new Technology(
                "Zero Cost",
                EnergyType.SOLAR,
                new Efficiency(60.0),
                new InstallationCost(BigDecimal.ONE), 
                new MaintenanceCost(BigDecimal.ONE),
                new EnvironmentalImpact(15.0),
                new Co2Reduction(5.0),
                new EnergyProduction(1000.0));

        List<Technology> technologies = List.of(zeroCostTech, solarTech());
        assertDoesNotThrow(() -> service.findMostEfficient(technologies));
    }
}
