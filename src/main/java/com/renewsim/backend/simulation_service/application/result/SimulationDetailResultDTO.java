package com.renewsim.backend.simulation_service.application.result;

import java.time.LocalDateTime;

public record SimulationDetailResultDTO(
    Long id,
    String location,
    String energyType,
    double projectSize,
    double budget,
    double energyGenerated,
    double estimatedSavings,
    Double returnOnInvestment,
    LocalDateTime timestamp
) {}


