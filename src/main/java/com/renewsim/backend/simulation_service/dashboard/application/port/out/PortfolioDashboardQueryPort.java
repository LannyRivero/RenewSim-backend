package com.renewsim.backend.simulation_service.dashboard.application.port.out;

import com.renewsim.backend.simulation_service.shared.application.SimulationReadModel;

import java.util.List;

public interface PortfolioDashboardQueryPort {

    List<SimulationReadModel> findByCreatedByOrderByCreatedAtDesc(String createdBy);
}
