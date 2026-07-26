package com.renewsim.backend.simulation_service.application.createSimulation;

import com.renewsim.backend.simulation_service.application.shared.SimulationDetailsResult;

public interface CreateRealSimulationUseCase {
    SimulationDetailsResult createSimulation(CreateRealSimulationCommand command);
}
