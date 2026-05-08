package com.renewsim.backend.simulation_service.web.dto;

import java.time.LocalDateTime;
import java.util.List;

// Temporary: Feign client integration pending

public record SimulationResponseDTO(
    Long simulationId,
    String location,
    String energyType,
    double energyGenerated,
    double estimatedSavings,
    double returnOnInvestment,
    double projectSize,
    double budget,
    LocalDateTime timestamp,
    List<TechnologyComparisonDTO> technologies,
    String recommendedTechnology
) {}
