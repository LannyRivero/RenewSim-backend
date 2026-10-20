package com.renewsim.backend.simulation_service.history.application.port.in;

import com.renewsim.backend.simulation_service.history.application.result.UserSimulationListResult;
import com.renewsim.backend.simulation_service.history.application.query.SimulationHistoryPageRequest;

public interface ListUserRealSimulationsUseCase {
    UserSimulationListResult getUserSimulations(String username, String location, SimulationHistoryPageRequest pageRequest);
}
