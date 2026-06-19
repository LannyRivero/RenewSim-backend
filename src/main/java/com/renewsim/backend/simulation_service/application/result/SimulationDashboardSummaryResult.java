package com.renewsim.backend.simulation_service.application.result;

import java.util.List;

public record SimulationDashboardSummaryResult(
        SimulationDashboardStatsResult stats,
        List<SimulationDashboardEnergyBySourceResult> energyBySource,
        List<SimulationDashboardEfficiencyMetricResult> efficiencyMetrics,
        List<SimulationDashboardTargetVsActualResult> targetVsActual) {
}