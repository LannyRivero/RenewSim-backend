package com.renewsim.backend.simulation_service.web.dto;

import java.time.LocalDateTime;

public record UserSimulationSummaryDTO(
    Long id,
    String name,
    String technology,
    double installedCapacity,
    double energyGenerated,
    double roi,
    String status,
    LocalDateTime createdAt,
    SimulationLocationSummaryDTO location
) {}
