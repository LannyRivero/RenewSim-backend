package com.renewsim.backend.simulation_service.history.application.port.in;

import com.renewsim.backend.simulation_service.history.application.result.UserSimulationListResult;

public interface ListUserRealSimulationsUseCase {
    UserSimulationListResult getUserSimulations(String username);
}
