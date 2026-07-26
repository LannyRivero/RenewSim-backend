package com.renewsim.backend.simulation_service.application.detailSimulation;

import com.renewsim.backend.simulation_service.application.shared.SimulationDetailsResult;

public interface GetRealSimulationUseCase {
    SimulationDetailsResult getSimulationById(Long id, String requesterUsername, boolean isAdmin);
}
