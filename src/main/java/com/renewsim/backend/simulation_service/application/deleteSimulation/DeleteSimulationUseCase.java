package com.renewsim.backend.simulation_service.application.deleteSimulation;

/**
 * Use case for deleting a Simulation by its ID.
 */
public interface DeleteSimulationUseCase {
    SimulationDeletionResultDTO deleteSimulation(DeleteSimulationCommand command);
}
