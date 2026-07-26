package com.renewsim.backend.simulation_service.application.dashboard;

public record PortfolioDashboardSummary(
        long totalSimulations,
        long activeSimulations,
        Double averageRoiPercent,
        Double medianPaybackYears,
        double totalEnergyGeneratedKwh,
        double totalCo2SavedKg,
        long atRiskCount) {
}
