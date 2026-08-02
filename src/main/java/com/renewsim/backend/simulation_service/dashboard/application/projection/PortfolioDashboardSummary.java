package com.renewsim.backend.simulation_service.dashboard.application.projection;

public record PortfolioDashboardSummary(
        long totalSimulations,
        long activeSimulations,
        Double averageRoiPercent,
        Double medianPaybackYears,
        double totalEnergyGeneratedKwh,
        double totalCo2SavedKg,
        long atRiskCount) {
}
