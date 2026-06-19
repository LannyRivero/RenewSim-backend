package com.renewsim.backend.simulation_service.web.dto;

public record DashboardEnergyBySourceResponseDTO(
        String label,
        double kwh) {
}
