package com.renewsim.backend.simulation_service.application.result;

import java.time.LocalDateTime;
import java.util.List;

public record SimulationDetailResultDTO(
    Long id,
    String location,
    String energyType,
    double projectSize,
    double budget,
    double energyGenerated,
    double estimatedSavings,
    Double returnOnInvestment,
    LocalDateTime timestamp,
    String createdBy,
    List<Long> technologyIds
) {}


