package com.renewsim.backend.simulation_service.dashboard.application.projection;

import java.util.List;

public record PortfolioDashboardResult(
        PortfolioDashboardSummary summary,
        PortfolioDashboardRecommendedScenario recommendedScenario,
        List<PortfolioDashboardPrioritizedScenario> prioritizedScenarios,
        List<PortfolioDashboardRiskAlert> riskAlerts,
        PortfolioDashboardDistribution distribution) {
}
