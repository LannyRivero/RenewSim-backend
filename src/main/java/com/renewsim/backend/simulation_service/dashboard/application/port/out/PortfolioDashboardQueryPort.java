package com.renewsim.backend.simulation_service.dashboard.application.port.out;

import com.renewsim.backend.simulation_service.domain.model.Simulation;

import java.util.List;

public interface PortfolioDashboardQueryPort {

    List<Simulation> findByCreatedByOrderByCreatedAtDesc(String createdBy);
}
