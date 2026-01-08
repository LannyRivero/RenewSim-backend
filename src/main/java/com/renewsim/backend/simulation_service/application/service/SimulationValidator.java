package com.renewsim.backend.simulation_service.application.service;

import org.springframework.stereotype.Component;
import com.renewsim.backend.simulation_service.domain.exception.InvalidSimulationParameterException;

@Component
public class SimulationValidator {

    public void validateProjectSize(double projectSize) {
        if (projectSize <= 0) {
            throw new InvalidSimulationParameterException("Project size must be greater than zero");
        }
    }

    public void validateBudget(double budget) {
        if (budget <= 0) {
            throw new InvalidSimulationParameterException("Budget must be greater than zero");
        }
    }
}
