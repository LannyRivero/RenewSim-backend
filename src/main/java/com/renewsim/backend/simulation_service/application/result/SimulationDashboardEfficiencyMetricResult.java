package com.renewsim.backend.simulation_service.application.result;

public record SimulationDashboardEfficiencyMetricResult(
        String label,
        String value,
        String hint) {
}