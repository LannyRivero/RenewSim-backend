package com.renewsim.backend.simulation_service.domain.model.vo;

import com.renewsim.backend.simulation_service.domain.exception.InvalidSimulationParameterException;

public record Budget(double value) {
    public Budget {
        if (value <= 0) {
            throw new InvalidSimulationParameterException("Budget must be greater than zero");
        }
    }
}

