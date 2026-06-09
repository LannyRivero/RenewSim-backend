package com.renewsim.backend.simulation_service.web.dto;

import java.time.LocalDateTime;
import java.util.List;

public record SimulationResultsResponseDTO(
    Long id,
    String name,
    LocalDateTime createdAt,
    SimulationLocationResponseDTO location,
    SimulationClimateDataResponseDTO climateData,
    String technology,
    double installedCapacity,
    double energyGenerated,
    double capacityFactor,
    SimulationFinancialsResponseDTO financials,
    List<Double> monthlyGeneration,
    List<Double> hourlyProfile
) {}
