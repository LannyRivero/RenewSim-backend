package com.renewsim.backend.simulation_service.application.result;

import java.time.LocalDateTime;

public record SimulationCreationResultDTO(
        Long id,
        String name,
        LocalDateTime createdAt) {
}
