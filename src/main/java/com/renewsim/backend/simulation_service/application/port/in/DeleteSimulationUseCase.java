package com.renewsim.backend.simulation_service.application.port.in;

import com.renewsim.backend.simulation_service.application.command.DeleteSimulationCommand;
import com.renewsim.backend.simulation_service.application.result.SimulationDeletionResultDTO;

/**
 * Use case for deleting a Simulation by its ID.
 */
public interface DeleteSimulationUseCase {
    SimulationDeletionResultDTO deleteSimulation(DeleteSimulationCommand command);
}
