package com.renewsim.backend.simulation_service.create.application.port.in;

import com.renewsim.backend.simulation_service.shared.application.SimulationDetailsResult;
import com.renewsim.backend.simulation_service.create.application.command.CreateRealSimulationCommand;

public interface CreateRealSimulationUseCase {
    SimulationDetailsResult createSimulation(CreateRealSimulationCommand command);
}
