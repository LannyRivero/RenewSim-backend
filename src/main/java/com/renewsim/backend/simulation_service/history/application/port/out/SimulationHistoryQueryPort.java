package com.renewsim.backend.simulation_service.history.application.port.out;

import com.renewsim.backend.simulation_service.domain.model.Simulation;

import java.util.List;

public interface SimulationHistoryQueryPort {

    List<Simulation> findByCreatedByOrderByCreatedAtDesc(String createdBy);
}
