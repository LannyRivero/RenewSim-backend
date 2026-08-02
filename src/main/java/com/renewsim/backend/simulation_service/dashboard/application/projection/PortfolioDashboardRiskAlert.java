package com.renewsim.backend.simulation_service.dashboard.application.projection;

public record PortfolioDashboardRiskAlert(
        String type,
        String severity,
        long count,
        String message) {
}
