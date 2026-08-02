package com.renewsim.backend.simulation_service.dashboard.application.projection;

public record PortfolioDashboardDistributionByTechnology(
        String label,
        long count,
        double energyKwh) {
}
