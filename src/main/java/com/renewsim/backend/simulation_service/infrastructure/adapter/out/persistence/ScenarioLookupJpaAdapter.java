package com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence;

import com.renewsim.backend.scenario_service.application.command.GetScenarioByIdCommand;
import com.renewsim.backend.scenario_service.application.port.in.GetScenarioUseCase;
import com.renewsim.backend.scenario_service.application.result.ScenarioResponseDTO;
import com.renewsim.backend.scenario_service.domain.exception.ScenarioNotFoundException;
import com.renewsim.backend.simulation_service.shared.application.port.out.ScenarioLookupPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ScenarioLookupJpaAdapter implements ScenarioLookupPort {

    private final GetScenarioUseCase getScenarioUseCase;

    @Override
    public Optional<ScenarioSnapshot> findActiveScenarioById(Long scenarioId) {
        try {
            ScenarioResponseDTO scenario = getScenarioUseCase.getScenarioById(new GetScenarioByIdCommand(scenarioId));
            return Optional.of(new ScenarioSnapshot(
                    scenario.id(),
                    scenario.name(),
                    scenario.technologyId(),
                    scenario.defaultCapacityKw(),
                    scenario.defaultInvestment().amount().doubleValue(),
                    scenario.defaultInvestment().currency(),
                    scenario.defaultTariff(),
                    scenario.defaultConsumption()));
        } catch (ScenarioNotFoundException ex) {
            return Optional.empty();
        }
    }
}
