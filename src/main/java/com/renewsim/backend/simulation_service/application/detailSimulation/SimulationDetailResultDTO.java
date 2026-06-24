package com.renewsim.backend.simulation_service.application.detailSimulation;

import java.time.LocalDateTime;
import java.util.List;

public record SimulationDetailResultDTO(
    Long id,
    String name,
    String location,
    double latitude,
    double longitude,
    String energyType,
    double installedCapacity,
    double budget,
    double energyGenerated,
    double capacityFactor,
    com.renewsim.backend.simulation_service.domain.model.vo.ClimateData climateData,
    LocalDateTime createdAt,
    String createdBy,
    List<Long> technologyIds
) {}


