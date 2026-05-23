package com.renewsim.backend.simulation_service.application.port.in;

import com.renewsim.backend.simulation_service.application.command.CreateSimulationCommand;
import com.renewsim.backend.simulation_service.application.result.SimulationCreationResultDTO;
import com.renewsim.backend.simulation_service.web.dto.SimulationRecommendationResultDTO;

public interface CreateSimulationUseCase {
    SimulationCreationResultDTO createSimulation(CreateSimulationCommand command);

    SimulationRecommendationResultDTO createSimulationWithRecommendation(CreateSimulationCommand command);
}

