package com.renewsim.backend.simulation_service.application.result;

public record SimulationDashboardStatsResult(
        int totalSimulations,
        Double totalEnergyGeneratedKwh,
        Double totalCo2SavedKg,
        Double averageRoiPercent) {
}