package com.renewsim.backend.simulation_service.domain.exception;

public class SimulationNotFoundException extends RuntimeException {
    public SimulationNotFoundException(Long id) {
        super("Simulation with ID " + id + " not found");
    }
}

