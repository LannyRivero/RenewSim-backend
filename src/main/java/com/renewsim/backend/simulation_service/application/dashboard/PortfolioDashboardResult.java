package com.renewsim.backend.simulation_service.application.dashboard;

import java.util.List;

public record PortfolioDashboardResult(
        PortfolioDashboardSummary summary,
        PortfolioDashboardRecommendedScenario recommendedScenario,
        List<PortfolioDashboardPrioritizedScenario> prioritizedScenarios,
        List<PortfolioDashboardRiskAlert> riskAlerts,
        PortfolioDashboardDistribution distribution) {
}
