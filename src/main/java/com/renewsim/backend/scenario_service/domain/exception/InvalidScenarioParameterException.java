package com.renewsim.backend.scenario_service.domain.exception;

public class InvalidScenarioParameterException extends RuntimeException {
    public InvalidScenarioParameterException(String message) {
        super(message);
    }
}
