package com.renewsim.backend.simulation_service.domain.exception;

public class InvalidSimulationParameterException extends RuntimeException {
    public InvalidSimulationParameterException(String message) {
        super(message);
    }
}
