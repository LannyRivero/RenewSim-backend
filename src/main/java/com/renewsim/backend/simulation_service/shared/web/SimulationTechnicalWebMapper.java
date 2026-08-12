package com.renewsim.backend.simulation_service.shared.web;

import com.renewsim.backend.simulation_service.shared.application.SimulationDetailsResult;
import com.renewsim.backend.simulation_service.shared.web.dto.SimulationDetailsResponseDTO;

final class SimulationTechnicalWebMapper {

        SimulationDetailsResponseDTO.TechnicalDTO toTechnical(SimulationDetailsResult.Technical technical) {
                return new SimulationDetailsResponseDTO.TechnicalDTO(
                                technical.annualGenerationKwh(),
                                technical.monthlyGenerationKwh(),
                                technical.specificYieldKwhPerKwp(),
                                technical.performanceRatio(),
                                technical.capacityFactorPct(),
                                technical.selfConsumptionRatePct(),
                                technical.coverageRatePct(),
                                new SimulationDetailsResponseDTO.ResourceSeriesDTO(
                                                technical.resource().source(),
                                                technical.resource().period(),
                                                technical.resource().monthlyIrradianceKwhM2(),
                                                technical.resource().monthlyTemperatureC()),
                                new SimulationDetailsResponseDTO.LossesSummaryDTO(
                                                technical.lossesPct().inverter(),
                                                technical.lossesPct().temperature(),
                                                technical.lossesPct().wiring(),
                                                technical.lossesPct().soiling(),
                                                technical.lossesPct().other(),
                                                technical.lossesPct().total()),
                                technical.balanceByMonth().stream()
                                                .map(item -> new SimulationDetailsResponseDTO.MonthlyEnergyBalanceItemDTO(
                                                                item.month(),
                                                                item.generationKwh(),
                                                                item.consumptionKwh(),
                                                                item.selfConsumedKwh(),
                                                                item.exportedKwh(),
                                                                item.importedKwh()))
                                                .toList());
        }
}
