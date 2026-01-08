package com.renewsim.backend.simulation_service.application.result;

/**
 * Result DTO returned after deleting a Simulation.
 */
public record SimulationDeletionResultDTO(
    Long id,
    boolean deleted,
    String message
) {}

