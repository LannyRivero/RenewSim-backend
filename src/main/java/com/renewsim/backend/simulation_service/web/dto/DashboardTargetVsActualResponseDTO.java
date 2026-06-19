package com.renewsim.backend.simulation_service.web.dto;

public record DashboardTargetVsActualResponseDTO(
        String label,
        double actual,
        double target,
        String unit) {
}
