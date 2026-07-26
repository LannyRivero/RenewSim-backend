package com.renewsim.backend.simulation_service.web.dto;

public record PortfolioDashboardSummaryDTO(
                long totalSimulations,
                long activeSimulations,
                Double averageRoiPercent,
                Double medianPaybackYears,
                double totalEnergyGeneratedKwh,
                double totalCo2SavedKg,
                long atRiskCount) {
}
