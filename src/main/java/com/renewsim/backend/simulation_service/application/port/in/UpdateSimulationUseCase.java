package com.renewsim.backend.simulation_service.application.port.in;

import com.renewsim.backend.simulation_service.application.command.UpdateSimulationCommand;

/**
 * Use case for updating an existing Simulation.
 */
public interface UpdateSimulationUseCase {
    SimulationUpdateResultDTO updateSimulation(UpdateSimulationCommand command);
}
