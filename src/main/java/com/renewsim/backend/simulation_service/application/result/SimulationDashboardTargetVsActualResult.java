package com.renewsim.backend.simulation_service.application.result;

public record SimulationDashboardTargetVsActualResult(
        String label,
        double actual,
        double target,
        String unit) {
}