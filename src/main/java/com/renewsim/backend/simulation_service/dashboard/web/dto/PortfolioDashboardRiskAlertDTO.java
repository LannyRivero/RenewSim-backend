package com.renewsim.backend.simulation_service.dashboard.web.dto;

public record PortfolioDashboardRiskAlertDTO(
                String type,
                String severity,
                long count,
                String message) {
}
