package com.renewsim.backend.simulation_service.application.port.in;

import com.renewsim.backend.simulation_service.application.command.CreateSimulationCommand;

public interface CreateSimulationUseCase {
    SimulationCreationResultDTO createSimulation(CreateSimulationCommand command);
}

