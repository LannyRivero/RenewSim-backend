package com.renewsim.backend.scenario_service.application.service;

import com.renewsim.backend.scenario_service.application.port.out.ScenarioRepositoryPort;
import com.renewsim.backend.scenario_service.application.port.out.ScenarioTechnologyLookupPort;
import com.renewsim.backend.scenario_service.domain.exception.ScenarioNotFoundException;
import com.renewsim.backend.scenario_service.domain.exception.ScenarioTechnologyNotFoundException;
import com.renewsim.backend.scenario_service.domain.model.Scenario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ScenarioValidator {

    private final ScenarioRepositoryPort scenarioRepository;
    private final ScenarioTechnologyLookupPort technologyLookupPort;

    public Scenario getExistingActiveScenario(Long id) {
        return scenarioRepository.findById(id)
                .filter(Scenario::isActive)
                .orElseThrow(() -> new ScenarioNotFoundException(id));
    }

    public void ensureActiveTechnologyExists(Long technologyId) {
        if (!technologyLookupPort.existsActiveTechnology(technologyId)) {
            throw new ScenarioTechnologyNotFoundException(technologyId);
        }
    }
}
