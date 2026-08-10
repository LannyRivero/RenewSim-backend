package com.renewsim.backend.simulation_service.create.application;

import com.renewsim.backend.simulation_service.create.application.command.CreateRealSimulationCommand;
import com.renewsim.backend.simulation_service.create.application.port.in.CreateRealSimulationUseCase;
import com.renewsim.backend.simulation_service.shared.application.port.out.SimulationRecordRepositoryPort;
import com.renewsim.backend.simulation_service.shared.application.port.out.TechnologyLookupPort;
import com.renewsim.backend.simulation_service.shared.application.SimulationDetailsResult;
import com.renewsim.backend.simulation_service.domain.exception.InvalidSimulationTechnologyException;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.simulation_service.domain.model.vo.Technology;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CreateSimulationService implements CreateRealSimulationUseCase {

    private final SimulationRecordRepositoryPort repository;
    private final TechnologyLookupPort technologyLookupPort;
    private final List<SimulationEngine> simulationEngines;
    private final SimulationCompletionMapper completionMapper;

    @Override
    public SimulationDetailsResult createSimulation(CreateRealSimulationCommand command) {
        validateTechnology(command.technology());
        SimulationEngine simulationEngine = resolveEngine(command.technology());
        simulationEngine.assertImplemented();

        List<Long> technologyIds = resolveTechnologyIds(command);
        Simulation simulation = persistDraft(command, technologyIds);
        SimulationDetailsResult result = simulationEngine.simulate(simulation, command);
        simulation.complete(completionMapper.toCompletion(result, technologyIds));
        repository.save(simulation);

        return result;
    }

    private Simulation persistDraft(CreateRealSimulationCommand command, List<Long> technologyIds) {
        return repository.save(SimulationCommandMapper.toNewSimulation(command, technologyIds));
    }

    private List<Long> resolveTechnologyIds(CreateRealSimulationCommand command) {
        if (command.technologyIds() != null && !command.technologyIds().isEmpty()) {
            return List.copyOf(command.technologyIds());
        }
        return technologyLookupPort.recommendActiveTechnologyIdsByEnergyType(command.technology().value());
    }

    private void validateTechnology(Technology technology) {
        if (!technologyLookupPort.existsActiveByEnergyType(technology.value())) {
            throw new InvalidSimulationTechnologyException(
                    "UNSUPPORTED_TECHNOLOGY: '" + technology.value()
                            + "' is not registered or is inactive in the technology catalog");
        }
    }

    private SimulationEngine resolveEngine(Technology technology) {
        return simulationEngines.stream()
                .filter(engine -> engine.supports(technology))
                .findFirst()
                .orElseThrow(() -> new InvalidSimulationTechnologyException(
                        "UNSUPPORTED_TECHNOLOGY: '" + technology.value() + "' is not implemented yet"));
    }
}
