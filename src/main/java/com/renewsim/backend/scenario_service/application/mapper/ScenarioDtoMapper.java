package com.renewsim.backend.scenario_service.application.mapper;

import com.renewsim.backend.scenario_service.application.result.ScenarioResponseDTO;
import com.renewsim.backend.scenario_service.domain.model.Scenario;
import org.springframework.stereotype.Component;

@Component
public class ScenarioDtoMapper {

    public ScenarioResponseDTO toResponse(Scenario scenario) {
        return new ScenarioResponseDTO(
                scenario.getId(),
                scenario.getName(),
                scenario.getDescription(),
                scenario.getTechnologyId(),
                scenario.getDefaultCapacityKw(),
                scenario.getDefaultInvestment(),
                scenario.getDefaultTariff(),
                scenario.getDefaultConsumption(),
                scenario.getClimateProfile(),
                scenario.isActive());
    }
}
