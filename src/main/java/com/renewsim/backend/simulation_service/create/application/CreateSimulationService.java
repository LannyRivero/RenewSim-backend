package com.renewsim.backend.simulation_service.create.application;

import com.renewsim.backend.simulation_service.create.application.command.CreateRealSimulationCommand;
import com.renewsim.backend.simulation_service.create.application.port.in.CreateRealSimulationUseCase;
import com.renewsim.backend.simulation_service.create.application.port.out.CreateSimulationRepositoryPort;
import com.renewsim.backend.simulation_service.domain.exception.InvalidSimulationTechnologyException;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.simulation_service.domain.model.vo.Technology;
import com.renewsim.backend.simulation_service.shared.application.SimulationDetailsResult;
import com.renewsim.backend.simulation_service.shared.application.SimulationUseCaseTelemetry;
import com.renewsim.backend.simulation_service.shared.application.port.out.TechnologyLookupPort;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CreateSimulationService implements CreateRealSimulationUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateSimulationService.class);
    private static final String USE_CASE = "create";

    private final CreateSimulationRepositoryPort repository;
    private final TechnologyLookupPort technologyLookupPort;
    private final List<SimulationEngine> simulationEngines;
    private final SimulationCompletionAssembler completionAssembler;
    private final SimulationUseCaseTelemetry telemetry;

    @Override
    public SimulationDetailsResult createSimulation(CreateRealSimulationCommand command) {
        Timer.Sample sample = telemetry.start();
        try {
            validateTechnology(command.technology());
            SimulationEngine simulationEngine = resolveEngine(command.technology());
            simulationEngine.assertImplemented();

            List<Long> technologyIds = resolveTechnologyIds(command);
            Simulation simulation = persistDraft(command, technologyIds);
            SimulationDetailsResult result = simulationEngine.simulate(simulation, command);
            simulation.complete(completionAssembler.toCompletion(result, technologyIds));
            repository.save(simulation);

            recordCreateSuccess(command, result, sample);

            return result;
        } catch (RuntimeException ex) {
            telemetry.recordError(USE_CASE, sample);
            log.info("Simulation create rejected user={} energyType={} scenarioOrigin={} reason={}",
                    command.createdBy(),
                    command.technology().value(),
                    command.scenarioId() != null,
                    ex.getClass().getSimpleName());
            throw ex;
        }
    }

    private void recordCreateSuccess(
            CreateRealSimulationCommand command,
            SimulationDetailsResult result,
            Timer.Sample sample) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            telemetry.recordSuccess(USE_CASE, sample);
            log.info("Simulation create succeeded user={} simulationId={} energyType={} scenarioOrigin={}",
                    command.createdBy(),
                    result.id(),
                    command.technology().value(),
                    command.scenarioId() != null);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                telemetry.recordSuccess(USE_CASE, sample);
                log.info("Simulation create succeeded user={} simulationId={} energyType={} scenarioOrigin={}",
                        command.createdBy(),
                        result.id(),
                        command.technology().value(),
                        command.scenarioId() != null);
            }

            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    telemetry.recordError(USE_CASE, sample);
                    log.warn("Simulation create rolled back user={} energyType={} scenarioOrigin={}",
                            command.createdBy(),
                            command.technology().value(),
                            command.scenarioId() != null);
                }
            }
        });
    }

    private Simulation persistDraft(CreateRealSimulationCommand command, List<Long> technologyIds) {
        return repository.save(SimulationCommandMapper.toNewSimulation(command, technologyIds));
    }

    private List<Long> resolveTechnologyIds(CreateRealSimulationCommand command) {
        if (command.technologyIds() != null && !command.technologyIds().isEmpty()) {
            validateTechnologyIds(command.technology(), command.technologyIds());
            return List.copyOf(command.technologyIds());
        }
        return technologyLookupPort.recommendActiveTechnologyIdsByEnergyType(command.technology().value());
    }

    private void validateTechnologyIds(Technology technology, List<Long> technologyIds) {
        if (new LinkedHashSet<>(technologyIds).size() != technologyIds.size()) {
            throw new InvalidSimulationTechnologyException(
                    "DUPLICATE_TECHNOLOGY_IDS: technologyIds must not contain duplicates");
        }

        for (Long technologyId : technologyIds) {
            String technologyEnergyType = technologyLookupPort.findActiveEnergyTypeByTechnologyId(technologyId)
                    .orElseThrow(() -> new InvalidSimulationTechnologyException(
                            "UNSUPPORTED_TECHNOLOGY_ID: '" + technologyId
                                    + "' is not registered or is inactive in the technology catalog"));

            if (!technology.value().equals(technologyEnergyType)) {
                throw new InvalidSimulationTechnologyException(
                        "INCOMPATIBLE_TECHNOLOGY_ID: '" + technologyId + "' does not belong to energyType '"
                                + technology.value() + "'");
            }
        }
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
