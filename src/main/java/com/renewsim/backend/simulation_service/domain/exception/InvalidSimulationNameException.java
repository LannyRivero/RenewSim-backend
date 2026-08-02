package com.renewsim.backend.simulation_service.domain.exception;

public class InvalidSimulationNameException extends RuntimeException {

    public InvalidSimulationNameException(String message) {
        super(message);
    }
}
