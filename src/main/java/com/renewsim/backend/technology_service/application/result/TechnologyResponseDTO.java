package com.renewsim.backend.technology_service.application.result;

import java.time.Instant;

public record TechnologyResponseDTO(
        Long id,
        String name,
        String energyType,
        double efficiency,
        double baseCostPerKw,
        int lifespanYears,
        double maintenanceCostPct,
        String description,
        boolean isActive,
        Instant createdAt,
        Instant updatedAt,
        double environmentalImpact,
        double co2Reduction,
        double capacityFactor
) {
}
