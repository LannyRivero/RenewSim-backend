package com.renewsim.backend.simulation_service.web.dto;

public record SimulationClimateDataResponseDTO(
    double irradiance,
    double windSpeed,
    double hydrology,
    Double temperature,
    String source,
    String period
) {}
