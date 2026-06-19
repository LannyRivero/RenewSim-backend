package com.renewsim.backend.simulation_service.web.dto;

import java.util.List;

public record DashboardSummaryResponseDTO(
        DashboardStatsResponseDTO stats,
        List<DashboardEnergyBySourceResponseDTO> energyBySource,
        List<DashboardEfficiencyMetricResponseDTO> efficiencyMetrics,
        List<DashboardTargetVsActualResponseDTO> targetVsActual) {
}
