package com.renewsim.backend.simulation_service.application.createSimulation;

import com.renewsim.backend.simulation_service.application.port.out.SimulationRecordRepositoryPort;
import com.renewsim.backend.simulation_service.application.port.out.TechnologyLookupPort;
import com.renewsim.backend.simulation_service.application.shared.SimulationDetailsResult;
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

        Simulation simulation = persistDraft(command);
        SimulationDetailsResult result = simulationEngine.simulate(simulation, command);
        simulation.complete(completionMapper.toCompletion(result));
        repository.save(simulation);

        return result;
    }

    private Simulation persistDraft(CreateRealSimulationCommand command) {
        return repository.save(SimulationCommandMapper.toNewSimulation(command));
    }

    private void validateTechnology(Technology technology) {
        if (!technologyLookupPort.existsActiveByEnergyType(technology.value())) {
            throw new InvalidSimulationTechnologyException(
                    "UNSUPPORTED_TECHNOLOGY: '" + technology.value() + "' is not registered or is inactive in the technology catalog");
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
