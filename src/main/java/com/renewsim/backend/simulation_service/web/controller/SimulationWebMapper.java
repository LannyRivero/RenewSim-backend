package com.renewsim.backend.simulation_service.web.controller;

import com.renewsim.backend.simulation_service.application.createSimulation.CreateRealSimulationCommand;
import com.renewsim.backend.simulation_service.application.historySimulation.UserSimulationListResult;
import com.renewsim.backend.simulation_service.application.shared.SimulationDetailsResult;
import com.renewsim.backend.simulation_service.domain.model.vo.ConsumptionProfile;
import com.renewsim.backend.simulation_service.domain.model.vo.CountryCode;
import com.renewsim.backend.simulation_service.domain.model.vo.Currency;
import com.renewsim.backend.simulation_service.domain.model.vo.ProjectLifetime;
import com.renewsim.backend.simulation_service.domain.model.vo.SimulationEconomics;
import com.renewsim.backend.simulation_service.domain.model.vo.SimulationLocation;
import com.renewsim.backend.simulation_service.domain.model.vo.SimulationSystem;
import com.renewsim.backend.simulation_service.domain.model.vo.Technology;
import com.renewsim.backend.simulation_service.web.dto.CreateSimulationRequestDTO;
import com.renewsim.backend.simulation_service.web.dto.ListUserSimulationsResponseDTO;
import com.renewsim.backend.simulation_service.web.dto.SimulationDetailsResponseDTO;
import com.renewsim.backend.simulation_service.web.dto.SimulationHistoryRowDTO;

/**
 * Maps simulation HTTP requests and responses to the application layer models.
 */
final class SimulationWebMapper {

    CreateRealSimulationCommand toCommand(CreateSimulationRequestDTO request, String username) {
        return new CreateRealSimulationCommand(
                request.name(),
                Technology.of(request.technology()),
                SimulationLocation.of(
                        request.location().label(),
                        request.location().lat(),
                        request.location().lon(),
                        request.location().country(),
                        CountryCode.of(request.location().countryCode())),
                new SimulationSystem(
                        request.system().installedCapacityKw(),
                        request.system().performanceRatio(),
                        request.system().degradationRateAnnualPct(),
                        request.system().availabilityPct(),
                        new SimulationSystem.LossesPct(
                                request.system().lossesPct().inverter(),
                                request.system().lossesPct().temperature(),
                                request.system().lossesPct().wiring(),
                                request.system().lossesPct().soiling(),
                                request.system().lossesPct().other())),
                ConsumptionProfile.of(
                        request.demand().annualConsumptionKwh(),
                        request.demand().monthlyConsumptionKwh()),
                new SimulationEconomics(
                        Currency.of(request.economics().currency()),
                        request.economics().capexTotal(),
                        request.economics().opexAnnual(),
                        request.economics().electricityPurchasePricePerKwh(),
                        request.economics().exportPricePerKwh(),
                        request.economics().discountRatePct(),
                        ProjectLifetime.of(request.economics().projectLifetimeYears())),
                username);
    }

    SimulationDetailsResponseDTO toWebDetails(SimulationDetailsResult result) {
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
                new CreateSimulationRequestDTO(
                        result.input().name(),
                        result.input().technology(),
                        new CreateSimulationRequestDTO.LocationDTO(
                                result.input().location().label(),
                                result.input().location().lat(),
                                result.input().location().lon(),
                                result.input().location().country(),
                                result.input().location().countryCode()),
                        new CreateSimulationRequestDTO.SystemDTO(
                                result.input().system().installedCapacityKw(),
                                result.input().system().performanceRatio(),
                                result.input().system().degradationRateAnnualPct(),
                                result.input().system().availabilityPct(),
                                new CreateSimulationRequestDTO.LossesPctDTO(
                                        result.input().system().lossesPct().inverter(),
                                        result.input().system().lossesPct().temperature(),
                                        result.input().system().lossesPct().wiring(),
                                        result.input().system().lossesPct().soiling(),
                                        result.input().system().lossesPct().other())),
                        new CreateSimulationRequestDTO.DemandDTO(
                                result.input().demand().annualConsumptionKwh(),
                                result.input().demand().monthlyConsumptionKwh()),
                        new CreateSimulationRequestDTO.EconomicsDTO(
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

    ListUserSimulationsResponseDTO toWebList(UserSimulationListResult result) {
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

}
