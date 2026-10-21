package com.renewsim.backend.simulation_service.history.application.port.out;

import com.renewsim.backend.simulation_service.shared.application.SimulationReadModel;
import com.renewsim.backend.simulation_service.history.application.query.SimulationHistoryPage;
import com.renewsim.backend.simulation_service.history.application.query.SimulationHistoryPageRequest;

import java.util.List;

public interface SimulationHistoryQueryPort {

    SimulationHistoryPage<SimulationReadModel> searchByCreatedBy(
            String createdBy, String name, String location, SimulationHistoryPageRequest pageRequest);

    List<String> findLocationSuggestionsByCreatedBy(String createdBy, String normalizedQuery, int limit);
}
