package com.renewsim.backend.simulation_service.web.dto;

import jakarta.validation.constraints.PositiveOrZero;

/**
 * Represents the normalization boundaries for energy, cost, CO₂ and efficiency metrics.
 * This DTO is used in runtime calculations to normalize and compare technologies.
 */
public record NormalizationStatsDTO(
    @PositiveOrZero double minCo2,
    @PositiveOrZero double maxCo2,
    @PositiveOrZero double minEnergy,
    @PositiveOrZero double maxEnergy,
    @PositiveOrZero double minCost,
    @PositiveOrZero double maxCost,
    @PositiveOrZero double minEfficiency,
    @PositiveOrZero double maxEfficiency
) {}
