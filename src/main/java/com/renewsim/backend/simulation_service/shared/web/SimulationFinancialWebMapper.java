package com.renewsim.backend.simulation_service.shared.web;

import com.renewsim.backend.simulation_service.shared.application.SimulationDetailsResult;
import com.renewsim.backend.simulation_service.shared.web.dto.SimulationDetailsResponseDTO;

import java.util.List;

final class SimulationFinancialWebMapper {

        SimulationDetailsResponseDTO.FinancialDTO toFinancial(SimulationDetailsResult.Financial financial) {
                return new SimulationDetailsResponseDTO.FinancialDTO(
                                financial.currency(),
                                financial.annualSavings(),
                                financial.annualExportRevenue(),
                                financial.netAnnualBenefit(),
                                financial.paybackYears(),
                                financial.discountedPaybackYears(),
                                financial.npv(),
                                financial.irrPct(),
                                financial.lcoePerKwh(),
                                financial.yearlyCashFlows().stream()
                                                .map(item -> new SimulationDetailsResponseDTO.FinancialYearItemDTO(
                                                                item.year(),
                                                                item.savings(),
                                                                item.exportRevenue(),
                                                                item.opex(),
                                                                item.replacementCost(),
                                                                item.netCashFlow(),
                                                                item.discountedCashFlow(),
                                                                item.cumulativeCashFlow()))
                                                .toList());
        }

        SimulationDetailsResponseDTO.AssumptionsDTO toAssumptions(SimulationDetailsResult.Assumptions assumptions) {
                return new SimulationDetailsResponseDTO.AssumptionsDTO(
                                assumptions.discountRatePct(),
                                assumptions.projectLifetimeYears(),
                                assumptions.degradationRateAnnualPct(),
                                assumptions.electricityPurchasePricePerKwh(),
                                assumptions.exportPricePerKwh(),
                                assumptions.climateSource(),
                                assumptions.climatePeriod());
        }

        List<SimulationDetailsResponseDTO.SimulationWarningDTO> toWarnings(
                        List<SimulationDetailsResult.SimulationWarning> warnings) {
                return warnings.stream()
                                .map(warning -> new SimulationDetailsResponseDTO.SimulationWarningDTO(
                                                warning.severity(),
                                                warning.code(),
                                                warning.message()))
                                .toList();
        }
}
