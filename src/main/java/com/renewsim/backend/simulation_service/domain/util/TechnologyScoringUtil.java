package com.renewsim.backend.simulation_service.domain.util;

import java.util.List;
import com.renewsim.backend.simulation_service.web.dto.NormalizationStatsDTO;

/**
 * Utility class for calculating technology scores based on CO2 reduction,
 * energy output, efficiency, and installation cost.
 * 
 * This class is pure domain logic — no external dependencies or framework bindings.
 */
public final class TechnologyScoringUtil {

    private static final double WEIGHT_CO2 = 0.25;
    private static final double WEIGHT_ENERGY = 0.30;
    private static final double WEIGHT_EFFICIENCY = 0.25;
    private static final double WEIGHT_COST = 0.20;

    private TechnologyScoringUtil() {}

    /**
     * Calculates normalization statistics for a list of technology metrics.
     */
    public static NormalizationStatsDTO calculateNormalizationStats(List<TechnologyData> technologies) {
        double minCo2 = technologies.stream().mapToDouble(TechnologyData::co2Reduction).min().orElse(0);
        double maxCo2 = technologies.stream().mapToDouble(TechnologyData::co2Reduction).max().orElse(1);

        double minEnergy = technologies.stream().mapToDouble(TechnologyData::energyProduction).min().orElse(0);
        double maxEnergy = technologies.stream().mapToDouble(TechnologyData::energyProduction).max().orElse(1);

        double minCost = technologies.stream().mapToDouble(TechnologyData::installationCost).min().orElse(0);
        double maxCost = technologies.stream().mapToDouble(TechnologyData::installationCost).max().orElse(1);

        double minEfficiency = technologies.stream().mapToDouble(TechnologyData::efficiency).min().orElse(0);
        double maxEfficiency = technologies.stream().mapToDouble(TechnologyData::efficiency).max().orElse(1);

        return new NormalizationStatsDTO(
            minCo2, maxCo2,
            minEnergy, maxEnergy,
            minCost, maxCost,
            minEfficiency, maxEfficiency
        );
    }

    /**
     * Calculates a weighted score dynamically for a single technology.
     */
    public static double calculateScoreDynamic(TechnologyData tech, NormalizationStatsDTO stats) {
        double normalizedCo2 = normalize(tech.co2Reduction(), stats.minCo2(), stats.maxCo2());
        double normalizedEnergy = normalize(tech.energyProduction(), stats.minEnergy(), stats.maxEnergy());
        double normalizedCost = normalize(tech.installationCost(), stats.minCost(), stats.maxCost());
        double normalizedEff = normalize(tech.efficiency(), stats.minEfficiency(), stats.maxEfficiency());

        return (normalizedCo2 * WEIGHT_CO2)
             + (normalizedEnergy * WEIGHT_ENERGY)
             + (normalizedEff * WEIGHT_EFFICIENCY)
             - (normalizedCost * WEIGHT_COST);
    }

    /**
     * Normalizes a metric between 0 and 1.
     */
    public static double normalize(double value, double min, double max) {
        if (max - min == 0) return 0.0;
        return (value - min) / (max - min);
    }

    /**
     * Internal DTO for technology metrics.
     * This avoids coupling with external services or modules.
     */
    public record TechnologyData(
        double co2Reduction,
        double energyProduction,
        double installationCost,
        double efficiency
    ) {}
}
