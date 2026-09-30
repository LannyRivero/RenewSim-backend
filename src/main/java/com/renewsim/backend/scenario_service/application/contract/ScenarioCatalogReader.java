package com.renewsim.backend.scenario_service.application.contract;

import java.util.Optional;

/**
 * Read-only catalog contract exposed by scenario_service for cross-context consumers.
 */
public interface ScenarioCatalogReader {

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
