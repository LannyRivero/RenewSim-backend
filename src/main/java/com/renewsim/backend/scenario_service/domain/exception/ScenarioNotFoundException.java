package com.renewsim.backend.scenario_service.domain.exception;

public class ScenarioNotFoundException extends RuntimeException {
    public ScenarioNotFoundException(Long id) {
        super("Scenario not found with id: " + id);
    }
}
