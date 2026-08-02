package com.renewsim.backend.simulation_service.dashboard.web.dto;

public record PortfolioDashboardDistributionByTechnologyDTO(
                String label,
                long count,
                double energyKwh) {
}
