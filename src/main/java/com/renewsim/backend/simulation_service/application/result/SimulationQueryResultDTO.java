package com.renewsim.backend.simulation_service.application.result;

import java.time.LocalDateTime;
import java.util.List;

public record SimulationQueryResultDTO(
    Long id,
    String location,
    String energyType,
    double projectSize,
    double budget,
    double estimatedEnergy,
    double co2Reduction,
    LocalDateTime createdAt,
    List<String> technologyIds,
    String createdBy
) {}
