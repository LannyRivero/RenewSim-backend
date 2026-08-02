package com.renewsim.backend.simulation_service.dashboard.web.dto;

import java.util.List;

public record PortfolioDashboardResponseDTO(
                PortfolioDashboardSummaryDTO summary,
                PortfolioDashboardRecommendedScenarioDTO recommendedScenario,
                List<PortfolioDashboardPrioritizedScenarioDTO> prioritizedScenarios,
                List<PortfolioDashboardRiskAlertDTO> riskAlerts,
                PortfolioDashboardDistributionDTO distribution) {
}
