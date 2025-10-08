package com.renewsim.backend.technology_service.application.result;

public record TechnologyUpdateResultDTO(
        Long id,
        String name,
        String energyType,
        double efficiency,
        double installationCost,
        double maintenanceCost,
        double environmentalImpact,
        double co2Reduction,
        double energyProduction,
        boolean success,
        String message
) {}

