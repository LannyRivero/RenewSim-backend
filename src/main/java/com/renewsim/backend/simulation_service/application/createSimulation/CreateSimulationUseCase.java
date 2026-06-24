package com.renewsim.backend.simulation_service.application.createSimulation;

import com.renewsim.backend.simulation_service.application.result.SimulationRecommendationResultDTO;

public interface CreateSimulationUseCase {
    SimulationCreationResultDTO createSimulation(CreateSimulationCommand command);

    SimulationRecommendationResultDTO createSimulationWithRecommendation(CreateSimulationCommand command);
}

