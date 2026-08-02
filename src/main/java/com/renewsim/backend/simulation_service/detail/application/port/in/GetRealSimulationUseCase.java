package com.renewsim.backend.simulation_service.detail.application.port.in;

import com.renewsim.backend.simulation_service.shared.application.SimulationDetailsResult;

public interface GetRealSimulationUseCase {
    SimulationDetailsResult getSimulationById(Long id, String requesterUsername, boolean isAdmin);
}
