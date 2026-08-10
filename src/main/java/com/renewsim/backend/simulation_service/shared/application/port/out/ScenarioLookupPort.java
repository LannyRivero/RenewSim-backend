package com.renewsim.backend.simulation_service.shared.application.port.out;

import java.util.Optional;

public interface ScenarioLookupPort {

    Optional<ScenarioSnapshot> findActiveScenarioById(Long scenarioId);

    record ScenarioSnapshot(
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
