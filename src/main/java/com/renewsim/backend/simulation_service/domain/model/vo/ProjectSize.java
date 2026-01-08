package com.renewsim.backend.simulation_service.domain.model.vo;

import com.renewsim.backend.simulation_service.domain.exception.InvalidSimulationParameterException;

public record ProjectSize(double value) {
    public ProjectSize {
        if (value <= 0) {
            throw new InvalidSimulationParameterException("Project size must be greater than zero");
        }
    }
}
