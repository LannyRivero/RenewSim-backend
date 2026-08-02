package com.renewsim.backend.simulation_service.dashboard.application.projection;

public record PortfolioDashboardPrioritizedScenario(
        String id,
        String name,
        String technology,
        String status,
        String location,
        Double roiPercent,
        Double paybackYears,
        Double capex,
        Double estimatedAnnualSavings,
        String priority,
        Integer score) {
}
