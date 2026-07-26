package com.renewsim.backend.simulation_service.application.dashboard;

public record PortfolioDashboardRiskAlert(
        String type,
        String severity,
        long count,
        String message) {
}
