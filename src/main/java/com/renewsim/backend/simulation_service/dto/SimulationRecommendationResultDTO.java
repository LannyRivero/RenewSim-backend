package com.renewsim.backend.simulation_service.dto;

import java.util.List;

public record SimulationRecommendationResultDTO(
        Long simulationId,
        String energyType,
        List<Long> recommendedTechnologyIds
) {}
