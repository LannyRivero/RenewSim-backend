package com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence;

import com.renewsim.backend.scenario_service.application.contract.ScenarioCatalogReader;
import com.renewsim.backend.simulation_service.shared.application.port.out.ScenarioLookupPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ScenarioLookupJpaAdapter implements ScenarioLookupPort {

    private final ScenarioCatalogReader scenarioCatalogReader;

    @Override
    public Optional<ScenarioSnapshot> findActiveScenarioById(Long scenarioId) {
        return scenarioCatalogReader.findActiveScenarioById(scenarioId)
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
