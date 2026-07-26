package com.renewsim.backend.simulation_service.application.dashboard;

public record PortfolioDashboardDistributionByTechnology(
        String label,
        long count,
        double energyKwh) {
}
