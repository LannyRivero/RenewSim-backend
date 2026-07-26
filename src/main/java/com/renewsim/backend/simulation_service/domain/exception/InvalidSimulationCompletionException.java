package com.renewsim.backend.simulation_service.domain.exception;

public class InvalidSimulationCompletionException extends RuntimeException {

    public InvalidSimulationCompletionException(String message) {
        super(message);
    }
}
