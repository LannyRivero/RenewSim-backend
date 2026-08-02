package com.renewsim.backend.simulation_service.dashboard.web;

import com.renewsim.backend.simulation_service.dashboard.application.projection.PortfolioDashboardResult;
import com.renewsim.backend.simulation_service.dashboard.web.dto.PortfolioDashboardDistributionByStatusDTO;
import com.renewsim.backend.simulation_service.dashboard.web.dto.PortfolioDashboardDistributionByTechnologyDTO;
import com.renewsim.backend.simulation_service.dashboard.web.dto.PortfolioDashboardDistributionDTO;
import com.renewsim.backend.simulation_service.dashboard.web.dto.PortfolioDashboardPrioritizedScenarioDTO;
import com.renewsim.backend.simulation_service.dashboard.web.dto.PortfolioDashboardRecommendedScenarioDTO;
import com.renewsim.backend.simulation_service.dashboard.web.dto.PortfolioDashboardResponseDTO;
import com.renewsim.backend.simulation_service.dashboard.web.dto.PortfolioDashboardRiskAlertDTO;
import com.renewsim.backend.simulation_service.dashboard.web.dto.PortfolioDashboardSummaryDTO;

public final class SimulationDashboardWebMapper {

    public PortfolioDashboardResponseDTO toWebDashboard(PortfolioDashboardResult result) {
        return new PortfolioDashboardResponseDTO(
                new PortfolioDashboardSummaryDTO(
                        result.summary().totalSimulations(),
                        result.summary().activeSimulations(),
                        result.summary().averageRoiPercent(),
                        result.summary().medianPaybackYears(),
                        result.summary().totalEnergyGeneratedKwh(),
                        result.summary().totalCo2SavedKg(),
                        result.summary().atRiskCount()),
                result.recommendedScenario() == null ? null : new PortfolioDashboardRecommendedScenarioDTO(
                        result.recommendedScenario().id(),
                        result.recommendedScenario().name(),
                        result.recommendedScenario().technology(),
                        result.recommendedScenario().location(),
                        result.recommendedScenario().roiPercent(),
                        result.recommendedScenario().paybackYears(),
                        result.recommendedScenario().capex(),
                        result.recommendedScenario().estimatedAnnualSavings(),
                        result.recommendedScenario().priority(),
                        result.recommendedScenario().headline(),
                        result.recommendedScenario().drivers(),
                        result.recommendedScenario().mainRisk(),
                        result.recommendedScenario().nextStep()),
                result.prioritizedScenarios().stream()
                        .map(item -> new PortfolioDashboardPrioritizedScenarioDTO(
                                item.id(),
                                item.name(),
                                item.technology(),
                                item.status(),
                                item.location(),
                                item.roiPercent(),
                                item.paybackYears(),
                                item.capex(),
                                item.estimatedAnnualSavings(),
                                item.priority(),
                                item.score()))
                        .toList(),
                result.riskAlerts().stream()
                        .map(alert -> new PortfolioDashboardRiskAlertDTO(
                                alert.type(),
                                alert.severity(),
                                alert.count(),
                                alert.message()))
                        .toList(),
                new PortfolioDashboardDistributionDTO(
                        result.distribution().byTechnology().stream()
                                .map(item -> new PortfolioDashboardDistributionByTechnologyDTO(
                                        item.label(),
                                        item.count(),
                                        item.energyKwh()))
                                .toList(),
                        result.distribution().byStatus().stream()
                                .map(item -> new PortfolioDashboardDistributionByStatusDTO(
                                        item.label(),
                                        item.count()))
                                .toList()));
    }
}
