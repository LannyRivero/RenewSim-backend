package com.renewsim.backend.technology_service.application.result;

public record TechnologyQueryResultDTO(
        Long id,
        String name,
        String energyType,
        double efficiency,
        double installationCost,
        double maintenanceCost,
        double environmentalImpact,
        double co2Reduction,
        double energyProduction
) {}

