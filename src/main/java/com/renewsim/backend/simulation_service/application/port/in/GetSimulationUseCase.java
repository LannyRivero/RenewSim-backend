package com.renewsim.backend.simulation_service.application.port.in;

import com.renewsim.backend.simulation_service.application.command.GetSimulationByIdCommand;
import com.renewsim.backend.simulation_service.application.detailSimulation.SimulationDetailResultDTO;

/**
 * Use case for retrieving a Simulation by ID.
 */
public interface GetSimulationUseCase {
    SimulationDetailResultDTO getSimulationById(GetSimulationByIdCommand command);
}

