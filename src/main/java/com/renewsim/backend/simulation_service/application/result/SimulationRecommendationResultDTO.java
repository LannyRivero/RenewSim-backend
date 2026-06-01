package com.renewsim.backend.simulation_service.application.result;

import java.util.List;

public record SimulationRecommendationResultDTO(
        Long simulationId,
        String energyType,
        List<Long> recommendedTechnologyIds
) {
}
