package com.renewsim.backend.simulation_service.web.dto;

public record DashboardStatsResponseDTO(
        int totalSimulations,
        Double totalEnergyGeneratedKwh,
        Double totalCo2SavedKg,
        Double averageRoiPercent) {
}
