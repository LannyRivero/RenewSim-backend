package com.renewsim.backend.simulation_service.application.result;

import java.time.LocalDateTime;

/**
 * Result DTO returned after updating an existing Simulation.
 */
public record SimulationUpdateResultDTO(
    Long id,
    String location,
    String energyType,
    double projectSize,
    double budget,
    double estimatedEnergy,
    double co2Reduction,
    LocalDateTime updatedAt
) {}
