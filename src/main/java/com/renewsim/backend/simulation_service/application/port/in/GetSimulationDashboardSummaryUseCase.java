package com.renewsim.backend.simulation_service.application.port.in;

import com.renewsim.backend.simulation_service.application.result.SimulationDashboardSummaryResult;

public interface GetSimulationDashboardSummaryUseCase {
    SimulationDashboardSummaryResult getDashboardSummary(String username);
}