package com.renewsim.backend.simulation_service.web.dto;

public record DashboardEfficiencyMetricResponseDTO(
        String label,
        String value,
        String hint) {
}
