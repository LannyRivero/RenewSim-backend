package com.renewsim.backend.simulation_service.application.result;

import java.time.LocalDateTime;

public record SimulationCreationResultDTO(
        Long id,
        String location,
        String energyType,
        double projectSize,
        double budget,
        LocalDateTime createdAt) {
}
