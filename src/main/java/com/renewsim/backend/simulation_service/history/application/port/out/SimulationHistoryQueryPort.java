package com.renewsim.backend.simulation_service.history.application.port.out;

import com.renewsim.backend.simulation_service.shared.application.SimulationReadModel;

import java.util.List;

public interface SimulationHistoryQueryPort {

    List<SimulationReadModel> findByCreatedByOrderByCreatedAtDesc(String createdBy);
}
