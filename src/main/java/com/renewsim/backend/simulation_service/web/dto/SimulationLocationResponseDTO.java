package com.renewsim.backend.simulation_service.web.dto;

public record SimulationLocationResponseDTO(
    String name,
    String country,
    double lat,
    double lon
) {}
