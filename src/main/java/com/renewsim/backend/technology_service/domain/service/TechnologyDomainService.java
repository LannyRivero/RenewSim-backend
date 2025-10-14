package com.renewsim.backend.technology_service.domain.service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.renewsim.backend.technology_service.domain.model.Technology;

/**
 * Domain service for renewable technologies.
 *
 * Encapsulates business rules related to efficiency, sustainability,
 * and cost-based comparisons of renewable technologies.
 *
 * Pure domain logic: no dependencies on frameworks or infrastructure.
 */
public class TechnologyDomainService {

    /**
     * Normalizes a numeric value to a [0–1] scale.
     * Returns 0 if max == min to avoid division by zero.
     */
    public double normalize(double value, double min, double max) {
        if (Double.isNaN(value) || Double.isNaN(min) || Double.isNaN(max))
            throw new IllegalArgumentException("Values for normalization cannot be NaN");

        if (max == min)
            return 0.0;
        if (value < min)
            return 0.0;
        if (value > max)
            return 1.0;

        return (value - min) / (max - min);
    }

    /**
     * Calculates a weighted performance score for a technology.
     * Higher efficiency and lower environmental impact increase the score.
     */
    public double calculateScore(Technology technology) {
        if (technology == null)
            throw new IllegalArgumentException("Technology cannot be null");

        double efficiencyWeight = 0.6;
        double environmentalWeight = 0.4;

        // Access Value Object values
        double efficiency = technology.getEfficiency().value();
        double impact = technology.getEnvironmentalImpact().value();

        // Weighted score: maximize efficiency, minimize impact
        return (efficiency * efficiencyWeight) - (impact * environmentalWeight);
    }

    /**
     * Returns the technology with the best efficiency-to-cost ratio.
     */
    public Optional<Technology> findMostEfficient(List<Technology> technologies) {
        if (technologies == null || technologies.isEmpty())
            return Optional.empty();

        return technologies.stream()
                .max(Comparator.comparingDouble(t -> {
                    double efficiency = t.getEfficiency().value();
                    double installCost = t.getInstallationCost().value().doubleValue();
                    double maintenanceCost = t.getMaintenanceCost().value().doubleValue();
                    double totalCost = installCost + maintenanceCost;

                    return totalCost > 0 ? efficiency / totalCost : 0.0;
                }));
    }

    /**
     * Returns the technology with the best overall score according to
     * calculateScore().
     */
    public Optional<Technology> findBestOverall(List<Technology> technologies) {
        if (technologies == null || technologies.isEmpty())
            return Optional.empty();

        return technologies.stream()
                .max(Comparator.comparingDouble(this::calculateScore));
    }
}
