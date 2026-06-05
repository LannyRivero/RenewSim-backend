package com.renewsim.backend.scenario_service.domain.exception;

public class ScenarioTechnologyNotFoundException extends RuntimeException {
    public ScenarioTechnologyNotFoundException(Long technologyId) {
        super("Scenario technology not found with id: " + technologyId);
    }
}
