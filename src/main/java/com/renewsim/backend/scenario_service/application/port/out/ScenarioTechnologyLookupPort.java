package com.renewsim.backend.scenario_service.application.port.out;

public interface ScenarioTechnologyLookupPort {

    boolean existsActiveTechnology(Long technologyId);
}
