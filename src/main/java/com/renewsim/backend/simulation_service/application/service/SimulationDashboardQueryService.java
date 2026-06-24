package com.renewsim.backend.simulation_service.application.service;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.renewsim.backend.simulation_service.application.port.in.GetSimulationDashboardSummaryUseCase;
import com.renewsim.backend.simulation_service.application.port.out.SimulationRepositoryPort;
import com.renewsim.backend.simulation_service.application.result.SimulationDashboardEfficiencyMetricResult;
import com.renewsim.backend.simulation_service.application.result.SimulationDashboardEnergyBySourceResult;
import com.renewsim.backend.simulation_service.application.result.SimulationDashboardStatsResult;
import com.renewsim.backend.simulation_service.application.result.SimulationDashboardSummaryResult;
import com.renewsim.backend.simulation_service.application.result.SimulationDashboardTargetVsActualResult;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.simulation_service.domain.model.vo.CO2Reduction;
import com.renewsim.backend.simulation_service.domain.model.vo.EnergyOutput;

import lombok.RequiredArgsConstructor;

/**
 * Read-only application service that aggregates dashboard metrics for a
 * user's simulations.
 *
 * <p>Keeps dashboard-specific query logic separate from simulation CRUD and
 * update workflows.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SimulationDashboardQueryService implements GetSimulationDashboardSummaryUseCase {

        private static final double HOURS_PER_YEAR = 8760.0;
        private static final double CO2_KG_PER_KWH = 0.7;

        private final SimulationRepositoryPort repository;
        private final SimulationCalculator calculator;

        @Override
        public SimulationDashboardSummaryResult getDashboardSummary(String username) {
                List<Simulation> simulations = repository.findAllByCreatedBy(username);
                if (simulations.isEmpty()) {
                        return new SimulationDashboardSummaryResult(
                                        new SimulationDashboardStatsResult(0, null, null, null),
                                        List.of(),
                                        List.of(),
                                        List.of());
                }

                double totalEnergyKwh = 0.0;
                double totalCo2Kg = 0.0;
                double totalRoi = 0.0;
                double totalCapacityFactor = 0.0;
                double totalInstalledKw = 0.0;
                double totalTechnicalMaxKwh = 0.0;
                int roiCount = 0;
                int positiveRoiCount = 0;

                Map<String, Double> energyBySource = new HashMap<>();

                for (Simulation simulation : simulations) {
                        EnergyOutput energy = simulation.energyOutput() != null
                                        ? simulation.energyOutput()
                                        : calculator.calculateEnergyOutput(simulation);

                        CO2Reduction co2 = simulation.co2Reduction() != null
                                        ? simulation.co2Reduction()
                                        : calculator.calculateCo2Reduction(energy);

                        Simulation completed = simulation.withCalculatedResults(energy, co2);

                        totalEnergyKwh += energy.kwhPerYear();
                        totalCo2Kg += co2.tonsPerYear() * 1000.0;

                        double installedKw = simulation.projectSize().value();
                        totalInstalledKw += installedKw;
                        totalTechnicalMaxKwh += installedKw * HOURS_PER_YEAR;

                        double capacityFactor = calculator.calculateCapacityFactor(simulation);
                        totalCapacityFactor += capacityFactor;

                        double roi = calculator.calculateRoiPercent(completed);
                        totalRoi += roi;
                        roiCount++;
                        if (roi >= 0) {
                                positiveRoiCount++;
                        }

                        String sourceLabel = mapEnergySourceLabel(simulation.energyType().name());
                        energyBySource.merge(sourceLabel, energy.kwhPerYear(), Double::sum);
                }

                Double averageRoi = roiCount > 0 ? totalRoi / roiCount : null;

                List<SimulationDashboardEnergyBySourceResult> energyBySourceResults = energyBySource.entrySet().stream()
                                .map(entry -> new SimulationDashboardEnergyBySourceResult(entry.getKey(), entry.getValue()))
                                .toList();

                double averageCapacityFactor = totalCapacityFactor / simulations.size();
                double specificYield = totalInstalledKw > 0 ? totalEnergyKwh / totalInstalledKw : 0.0;
                double positiveRoiShare = (positiveRoiCount * 100.0) / simulations.size();

                List<SimulationDashboardEfficiencyMetricResult> efficiencyMetrics = List.of(
                                new SimulationDashboardEfficiencyMetricResult(
                                                "Capacity factor",
                                                String.format(Locale.US, "%.1f%%", averageCapacityFactor),
                                                "Average utilization across all simulations"),
                                new SimulationDashboardEfficiencyMetricResult(
                                                "Specific yield",
                                                String.format(Locale.US, "%.0f kWh/kW-year", specificYield),
                                                "Annual energy generated per installed kW"),
                                new SimulationDashboardEfficiencyMetricResult(
                                                "Positive ROI",
                                                String.format(Locale.US, "%.1f%%", positiveRoiShare),
                                                "Share of simulations with non-negative annual ROI"));

                List<SimulationDashboardTargetVsActualResult> targetVsActual = List.of(
                                new SimulationDashboardTargetVsActualResult(
                                                "Energy output",
                                                totalEnergyKwh,
                                                totalTechnicalMaxKwh,
                                                "kWh"),
                                new SimulationDashboardTargetVsActualResult(
                                                "CO2 reduction",
                                                totalCo2Kg,
                                                totalTechnicalMaxKwh * CO2_KG_PER_KWH,
                                                "kg"),
                                new SimulationDashboardTargetVsActualResult(
                                                "ROI",
                                                averageRoi != null ? averageRoi : 0.0,
                                                0.0,
                                                "%"));

                return new SimulationDashboardSummaryResult(
                                new SimulationDashboardStatsResult(
                                                simulations.size(),
                                                totalEnergyKwh,
                                                totalCo2Kg,
                                                averageRoi),
                                energyBySourceResults,
                                efficiencyMetrics,
                                targetVsActual);
        }

        private String mapEnergySourceLabel(String energyType) {
                return switch (energyType) {
                        case "SOLAR" -> "Solar";
                        case "WIND" -> "Wind";
                        case "HYDRO" -> "Hydroelectric";
                        case "HYBRID" -> "Hybrid";
                        default -> energyType;
                };
        }
}
