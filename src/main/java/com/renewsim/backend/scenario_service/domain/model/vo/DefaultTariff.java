package com.renewsim.backend.scenario_service.domain.model.vo;

import com.renewsim.backend.scenario_service.domain.exception.InvalidScenarioParameterException;

public record DefaultTariff(double value) {
    public DefaultTariff {
        if (value < 0) {
            throw new InvalidScenarioParameterException("Default tariff cannot be negative");
        }
    }
}
