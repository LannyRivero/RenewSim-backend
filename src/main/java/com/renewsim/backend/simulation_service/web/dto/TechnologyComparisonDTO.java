package com.renewsim.backend.simulation_service.web.dto;

/**
 * Local DTO representing technology comparison data
 * within the Simulation context.
 * 
 * ⚠️ Temporary placeholder until Feign integration with technology_service is active.
 */
public record TechnologyComparisonDTO(
    Long technologyId,
    String name,
    String energyType,
    double efficiency,
    double installationCost,
    double co2Reduction,
    double capacityFactor,
    double normalizedScore
) {}
