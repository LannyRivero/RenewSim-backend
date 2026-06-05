package com.renewsim.backend.scenario_service.domain.model.vo;

import com.renewsim.backend.scenario_service.domain.exception.InvalidScenarioParameterException;

public record DefaultConsumption(double value) {
    public DefaultConsumption {
        if (value < 0) {
            throw new InvalidScenarioParameterException("Default consumption cannot be negative");
        }
    }
}
