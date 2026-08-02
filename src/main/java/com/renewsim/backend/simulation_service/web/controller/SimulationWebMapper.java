package com.renewsim.backend.simulation_service.web.controller;

import com.renewsim.backend.simulation_service.application.dashboard.PortfolioDashboardResult;
import com.renewsim.backend.simulation_service.application.historySimulation.UserSimulationListResult;
import com.renewsim.backend.simulation_service.application.shared.SimulationDetailsResult;
import com.renewsim.backend.simulation_service.create.web.dto.CreateSolarSimulationRequestDTO;
import com.renewsim.backend.simulation_service.dashboard.web.dto.PortfolioDashboardDistributionByStatusDTO;
import com.renewsim.backend.simulation_service.dashboard.web.dto.PortfolioDashboardDistributionByTechnologyDTO;
import com.renewsim.backend.simulation_service.dashboard.web.dto.PortfolioDashboardDistributionDTO;
import com.renewsim.backend.simulation_service.dashboard.web.dto.PortfolioDashboardPrioritizedScenarioDTO;
import com.renewsim.backend.simulation_service.dashboard.web.dto.PortfolioDashboardRecommendedScenarioDTO;
import com.renewsim.backend.simulation_service.dashboard.web.dto.PortfolioDashboardResponseDTO;
import com.renewsim.backend.simulation_service.dashboard.web.dto.PortfolioDashboardRiskAlertDTO;
import com.renewsim.backend.simulation_service.dashboard.web.dto.PortfolioDashboardSummaryDTO;
import com.renewsim.backend.simulation_service.detail.web.dto.SimulationDetailsResponseDTO;
import com.renewsim.backend.simulation_service.history.web.dto.ListUserSimulationsResponseDTO;
import com.renewsim.backend.simulation_service.history.web.dto.SimulationHistoryRowDTO;

/**
 * Maps simulation HTTP requests and responses to the application layer models.
 */
public final class SimulationWebMapper {

    public SimulationDetailsResponseDTO toWebDetails(SimulationDetailsResult result) {
        return new SimulationDetailsResponseDTO(
                result.id(),
                result.status(),
                result.createdAt(),
                result.updatedAt(),
                result.modelVersion(),
                result.technology(),
                new SimulationDetailsResponseDTO.ResolvedLocationDTO(
                        result.location().label(),
                        result.location().name(),
                        result.location().adminRegion(),
                        result.location().country(),
                        result.location().countryCode(),
                        result.location().lat(),
                        result.location().lon(),
                        result.location().timezone()),
                new SimulationDetailsResponseDTO.SummaryDTO(
                        result.summary().recommendation(),
                        result.summary().headline(),
                        result.summary().summary(),
                        result.summary().reasons().stream()
                                .map(reason -> new SimulationDetailsResponseDTO.RecommendationReasonDTO(
                                        reason.area(),
                                        reason.severity(),
                                        reason.message()))
                                .toList()),
                new CreateSolarSimulationRequestDTO(
                        result.input().name(),
                        new CreateSolarSimulationRequestDTO.LocationDTO(
                                result.input().location().label(),
                                result.input().location().lat(),
                                result.input().location().lon(),
                                result.input().location().country(),
                                result.input().location().countryCode()),
                        new CreateSolarSimulationRequestDTO.SolarSystemDTO(
                                result.input().system().installedCapacityKw(),
                                result.input().system().performanceRatio(),
                                result.input().system().degradationRateAnnualPct(),
                                result.input().system().availabilityPct(),
                                new CreateSolarSimulationRequestDTO.LossesPctDTO(
                                        result.input().system().lossesPct().inverter(),
                                        result.input().system().lossesPct().temperature(),
                                        result.input().system().lossesPct().wiring(),
                                        result.input().system().lossesPct().soiling(),
                                        result.input().system().lossesPct().other())),
                        new CreateSolarSimulationRequestDTO.DemandDTO(
                                result.input().demand().annualConsumptionKwh(),
                                result.input().demand().monthlyConsumptionKwh()),
                        new CreateSolarSimulationRequestDTO.EconomicsDTO(
                                result.input().economics().currency(),
                                result.input().economics().capexTotal(),
                                result.input().economics().opexAnnual(),
                                result.input().economics().electricityPurchasePricePerKwh(),
                                result.input().economics().exportPricePerKwh(),
                                result.input().economics().discountRatePct(),
                                result.input().economics().projectLifetimeYears())),
                new SimulationDetailsResponseDTO.TechnicalDTO(
                        result.technical().annualGenerationKwh(),
                        result.technical().monthlyGenerationKwh(),
                        result.technical().specificYieldKwhPerKwp(),
                        result.technical().performanceRatio(),
                        result.technical().capacityFactorPct(),
                        result.technical().selfConsumptionRatePct(),
                        result.technical().coverageRatePct(),
                        new SimulationDetailsResponseDTO.ResourceSeriesDTO(
                                result.technical().resource().source(),
                                result.technical().resource().period(),
                                result.technical().resource().monthlyIrradianceKwhM2(),
                                result.technical().resource().monthlyTemperatureC()),
                        new SimulationDetailsResponseDTO.LossesSummaryDTO(
                                result.technical().lossesPct().inverter(),
                                result.technical().lossesPct().temperature(),
                                result.technical().lossesPct().wiring(),
                                result.technical().lossesPct().soiling(),
                                result.technical().lossesPct().other(),
                                result.technical().lossesPct().total()),
                        result.technical().balanceByMonth().stream()
                                .map(item -> new SimulationDetailsResponseDTO.MonthlyEnergyBalanceItemDTO(
                                        item.month(),
                                        item.generationKwh(),
                                        item.consumptionKwh(),
                                        item.selfConsumedKwh(),
                                        item.exportedKwh(),
                                        item.importedKwh()))
                                .toList()),
                new SimulationDetailsResponseDTO.FinancialDTO(
                        result.financial().currency(),
                        result.financial().annualSavings(),
                        result.financial().annualExportRevenue(),
                        result.financial().netAnnualBenefit(),
                        result.financial().paybackYears(),
                        result.financial().discountedPaybackYears(),
                        result.financial().npv(),
                        result.financial().irrPct(),
                        result.financial().lcoePerKwh(),
                        result.financial().yearlyCashFlows().stream()
                                .map(item -> new SimulationDetailsResponseDTO.FinancialYearItemDTO(
                                        item.year(),
                                        item.savings(),
                                        item.exportRevenue(),
                                        item.opex(),
                                        item.replacementCost(),
                                        item.netCashFlow(),
                                        item.discountedCashFlow(),
                                        item.cumulativeCashFlow()))
                                .toList()),
                new SimulationDetailsResponseDTO.AssumptionsDTO(
                        result.assumptions().discountRatePct(),
                        result.assumptions().projectLifetimeYears(),
                        result.assumptions().degradationRateAnnualPct(),
                        result.assumptions().electricityPurchasePricePerKwh(),
                        result.assumptions().exportPricePerKwh(),
                        result.assumptions().climateSource(),
                        result.assumptions().climatePeriod()),
                result.warnings().stream()
                        .map(warning -> new SimulationDetailsResponseDTO.SimulationWarningDTO(
                                warning.severity(),
                                warning.code(),
                                warning.message()))
                        .toList());
    }

    public ListUserSimulationsResponseDTO toWebList(UserSimulationListResult result) {
        return new ListUserSimulationsResponseDTO(
                result.items().stream()
                        .map(item -> new SimulationHistoryRowDTO(
                                item.id(),
                                item.name(),
                                item.technology(),
                                item.status(),
                                item.createdAt(),
                                item.locationLabel(),
                                item.annualGenerationKwh(),
                                item.annualSavings(),
                                item.npv(),
                                item.irrPct(),
                                item.recommendation(),
                                item.modelVersion(),
                                item.resourceSource()))
                        .toList(),
                result.total());
    }

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
