package com.renewsim.backend.simulation_service.application.result;

import java.time.LocalDateTime;

public record SimulationHistoryResultDTO(
        Long id,
        String location,
        String energyType,
        double energyGenerated,
        double estimatedSavings,
        Double roiYears,
        LocalDateTime createdAt
) {}
