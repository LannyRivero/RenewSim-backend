package com.renewsim.backend.simulation_service.domain.exception;

import com.renewsim.backend.shared.exception.ResourceNotFoundException;

public class SimulationScenarioNotFoundException extends ResourceNotFoundException {

    public SimulationScenarioNotFoundException(Long scenarioId) {
        super("Scenario with id '" + scenarioId + "' was not found or is not active");
    }
}
