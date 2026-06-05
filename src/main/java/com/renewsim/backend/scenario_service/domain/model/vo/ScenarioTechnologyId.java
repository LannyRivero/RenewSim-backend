package com.renewsim.backend.scenario_service.domain.model.vo;

import com.renewsim.backend.scenario_service.domain.exception.InvalidScenarioParameterException;

public record ScenarioTechnologyId(Long value) {
    public ScenarioTechnologyId {
        if (value == null || value <= 0) {
            throw new InvalidScenarioParameterException("Technology id must be greater than zero");
        }
    }
}
