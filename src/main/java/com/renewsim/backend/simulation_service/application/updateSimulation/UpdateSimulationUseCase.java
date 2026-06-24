package com.renewsim.backend.simulation_service.application.updateSimulation;

/**
 * Use case for updating an existing Simulation.
 */
public interface UpdateSimulationUseCase {
    SimulationUpdateResultDTO updateSimulation(UpdateSimulationCommand command);
}
