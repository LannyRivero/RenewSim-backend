package com.renewsim.backend.simulation_service.history.application.port.in;

import com.renewsim.backend.simulation_service.history.application.result.UserSimulationListResult;
import com.renewsim.backend.simulation_service.history.application.query.SimulationHistoryPageRequest;

import java.util.List;

public interface ListUserRealSimulationsUseCase {
    UserSimulationListResult getUserSimulations(
            String username, String name, String location, SimulationHistoryPageRequest pageRequest);

    List<String> getUserLocationSuggestions(String username, String query, int limit);
}
