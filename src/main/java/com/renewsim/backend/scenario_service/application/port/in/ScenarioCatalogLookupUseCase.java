package com.renewsim.backend.scenario_service.application.port.in;

import java.util.Optional;

public interface ScenarioCatalogLookupUseCase {

    Optional<ScenarioCatalogSnapshot> findActiveScenarioById(Long scenarioId);

    record ScenarioCatalogSnapshot(
            Long id,
            String name,
            Long technologyId,
            double defaultCapacityKw,
            double defaultInvestmentAmount,
            String defaultInvestmentCurrency,
            double defaultTariff,
            double defaultConsumption) {
    }
}
