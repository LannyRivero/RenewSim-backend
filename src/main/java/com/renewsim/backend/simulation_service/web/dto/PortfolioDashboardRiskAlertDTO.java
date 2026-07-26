package com.renewsim.backend.simulation_service.web.dto;

public record PortfolioDashboardRiskAlertDTO(
                String type,
                String severity,
                long count,
                String message) {
}
