package com.renewsim.backend.simulation_service.application.result;

public record SimulationDashboardEnergyBySourceResult(
        String label,
        double kwh) {
}