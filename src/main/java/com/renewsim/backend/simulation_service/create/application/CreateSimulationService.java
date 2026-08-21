package com.renewsim.backend.simulation_service.create.application;

import com.renewsim.backend.simulation_service.create.application.command.CreateRealSimulationCommand;
import com.renewsim.backend.simulation_service.create.application.port.in.CreateRealSimulationUseCase;
import com.renewsim.backend.simulation_service.create.application.port.out.CreateSimulationRepositoryPort;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.simulation_service.shared.application.SimulationBusinessTelemetry;
import com.renewsim.backend.simulation_service.shared.application.SimulationDetailsResult;
import com.renewsim.backend.simulation_service.shared.application.SimulationTechnologySupport;
import com.renewsim.backend.simulation_service.shared.application.SimulationUseCaseTelemetry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Use case that creates a real simulation from a client command.
 *
 * <p>
 * Orchestrates the create flow: validates the requested technology against the
 * catalog,
 * resolves the matching {@link SimulationEngine}, persists the simulation in
 * draft state,
 * runs the engine to compute results, and completes the aggregate with those
 * results before
 * persisting again. The draft-then-complete persistence keeps the stored
 * snapshot aligned
 * with the created (non-terminal) state.
 * </p>
 *
 * <p>
 * Success telemetry and business metrics are deliberately recorded only after
 * the enclosing
 * transaction commits (or immediately when no transaction is active). This
 * prevents counters
 * from counting simulations that later roll back, keeping the operational
 * metrics honest.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CreateSimulationService implements CreateRealSimulationUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreateSimulationService.class);
    private static final String USE_CASE = "create";

    private final CreateSimulationRepositoryPort repository;
    private final SimulationCompletionAssembler completionAssembler;
    private final SimulationUseCaseTelemetry telemetry;
    private final SimulationBusinessTelemetry businessTelemetry;
    private final SimulationTechnologySupport technologySupport;

    @Override
    public SimulationDetailsResult createSimulation(CreateRealSimulationCommand command) {
        Timer.Sample sample = telemetry.start();
        try {
            SimulationTechnologySupport.ResolvedTechnologyContext resolved = technologySupport.resolve(
                    command.technology(),
                    command.technologyIds());
            SimulationEngine simulationEngine = resolved.engine();
            List<Long> technologyIds = resolved.technologyIds();
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
            recordBusinessMetrics(command, result);
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
                recordBusinessMetrics(command, result);
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

    private void recordBusinessMetrics(CreateRealSimulationCommand command, SimulationDetailsResult result) {
        businessTelemetry.recordCreated(command.technology().value(), command.scenarioId() != null);
        businessTelemetry.recordRecommendation(command.technology().value(), result.summary().recommendation());
        businessTelemetry.recordAttention(command.technology().value(), result);
    }

    private Simulation persistDraft(CreateRealSimulationCommand command, List<Long> technologyIds) {
        return repository.save(SimulationCommandMapper.toNewSimulation(command, technologyIds));
    }

}
