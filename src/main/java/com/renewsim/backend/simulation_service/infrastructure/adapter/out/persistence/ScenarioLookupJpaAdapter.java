package com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence;

import com.renewsim.backend.scenario_service.application.port.in.ScenarioCatalogLookupUseCase;
import com.renewsim.backend.simulation_service.shared.application.port.out.ScenarioLookupPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ScenarioLookupJpaAdapter implements ScenarioLookupPort {

    private final ScenarioCatalogLookupUseCase scenarioCatalogLookupUseCase;

    @Override
    public Optional<ScenarioSnapshot> findActiveScenarioById(Long scenarioId) {
        return scenarioCatalogLookupUseCase.findActiveScenarioById(scenarioId)
                .map(scenario -> new ScenarioSnapshot(
                        scenario.id(),
                        scenario.name(),
                        scenario.technologyId(),
                        scenario.defaultCapacityKw(),
                        scenario.defaultInvestmentAmount(),
                        scenario.defaultInvestmentCurrency(),
                        scenario.defaultTariff(),
                        scenario.defaultConsumption()));
    }
}
