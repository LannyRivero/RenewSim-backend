package com.renewsim.backend.technology_service.application.result;

public record TechnologyCreationResultDTO(
        Long id,
        String name,
        String energyType,
        double efficiency,
        double installationCost,
        double maintenanceCost,
        double environmentalImpact,
        double co2Reduction,
        double capacityFactor,
        boolean success,
        String message
) {}
