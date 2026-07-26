package com.renewsim.backend.simulation_service.application.dashboard;

import java.util.List;

public record PortfolioDashboardRecommendedScenario(
        String id,
        String name,
        String technology,
        String location,
        Double roiPercent,
        Double paybackYears,
        Double capex,
        Double estimatedAnnualSavings,
        String priority,
        String headline,
        List<String> drivers,
        String mainRisk,
        String nextStep) {
}
