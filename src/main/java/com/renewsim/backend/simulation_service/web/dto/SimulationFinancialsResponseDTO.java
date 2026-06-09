package com.renewsim.backend.simulation_service.web.dto;

public record SimulationFinancialsResponseDTO(
    double capex,
    double opex,
    double revenue,
    double roi,
    double paybackYears,
    double npv,
    double irr
) {}
