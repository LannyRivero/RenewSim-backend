package com.renewsim.backend.simulation_service.application.historySimulation;

import java.time.LocalDateTime;

public record SimulationHistoryResultDTO(
        Long id,
        String name,
        String location,
        String country,
        double latitude,
        double longitude,
        String energyType,
        double installedCapacity,
        double energyGenerated,
        double roi,
        String status,
        LocalDateTime createdAt
) {}
