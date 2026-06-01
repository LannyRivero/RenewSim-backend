package com.renewsim.backend.simulation_service.web.dto;

public record TechnologyResponseDTO(
        Long id,
        String name,
        String energyType,
        double efficiency,
        double installationCost,
        double co2Reduction,
        double capacityFactor) {
}
