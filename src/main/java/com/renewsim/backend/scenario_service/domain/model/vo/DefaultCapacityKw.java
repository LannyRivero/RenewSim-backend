package com.renewsim.backend.scenario_service.domain.model.vo;

import com.renewsim.backend.scenario_service.domain.exception.InvalidScenarioParameterException;

public record DefaultCapacityKw(double value) {
    public DefaultCapacityKw {
        if (value <= 0) {
            throw new InvalidScenarioParameterException("Default capacity must be greater than zero");
        }
    }
}
