package com.renewsim.backend.simulation_service.domain.exception;

public class InvalidSimulationIdException extends RuntimeException {

    public InvalidSimulationIdException(Long value) {
        super("Simulation ID must be a positive number: " + value);
    }
}
