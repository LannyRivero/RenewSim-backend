package com.renewsim.backend.simulation_service.create.application;

import com.renewsim.backend.simulation_service.create.application.command.CreateRealSimulationCommand;
import com.renewsim.backend.simulation_service.create.application.command.CreateSimulationFromScenarioCommand;
import com.renewsim.backend.simulation_service.create.application.port.in.CreateRealSimulationUseCase;
import com.renewsim.backend.simulation_service.create.application.port.in.CreateSimulationFromScenarioUseCase;
import com.renewsim.backend.simulation_service.domain.exception.InvalidSimulationTechnologyException;
import com.renewsim.backend.simulation_service.domain.exception.SimulationScenarioNotFoundException;
import com.renewsim.backend.simulation_service.shared.application.SimulationDetailsResult;
import com.renewsim.backend.simulation_service.shared.application.port.out.ScenarioLookupPort;
import com.renewsim.backend.simulation_service.shared.application.port.out.TechnologyLookupPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Use case that turns a predefined scenario into a real simulation.
 *
 * <p>
 * Resolves the active scenario, derives its energy type from the referenced
 * technology,
 * recommends the matching technology ids, and adapts the scenario inputs into a
 * {@code CreateRealSimulationCommand} before delegating to the standard create
 * use case. This
 * keeps the real-simulation flow as the single place where engines and
 * persistence run.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CreateSimulationFromScenarioService implements CreateSimulationFromScenarioUseCase {

        private final ScenarioLookupPort scenarioLookupPort;
        private final TechnologyLookupPort technologyLookupPort;
        private final CreateRealSimulationUseCase createRealSimulationUseCase;
        private final ScenarioSimulationCommandFactory scenarioSimulationCommandFactory;

        @Override
        public SimulationDetailsResult createSimulationFromScenario(CreateSimulationFromScenarioCommand command) {
                ScenarioLookupPort.ScenarioSnapshot scenario = scenarioLookupPort
                                .findActiveScenarioById(command.scenarioId())
                                .orElseThrow(() -> new SimulationScenarioNotFoundException(command.scenarioId()));

                String energyType = technologyLookupPort.findActiveEnergyTypeByTechnologyId(scenario.technologyId())
                                .orElseThrow(() -> new InvalidSimulationTechnologyException(
                                                "UNSUPPORTED_SCENARIO_TECHNOLOGY: technologyId '"
                                                                + scenario.technologyId()
                                                                + "' is not registered or is inactive in the technology catalog"));

                List<Long> technologyIds = technologyLookupPort.recommendActiveTechnologyIdsByEnergyType(energyType);

                CreateRealSimulationCommand realCommand = scenarioSimulationCommandFactory.fromScenario(
                                command,
                                scenario,
                                energyType,
                                technologyIds);

                return createRealSimulationUseCase.createSimulation(realCommand);
        }
}
