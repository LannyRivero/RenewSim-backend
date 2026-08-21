package com.renewsim.backend.simulation_service.update.application;

import com.renewsim.backend.simulation_service.create.application.SimulationCompletionAssembler;
import com.renewsim.backend.simulation_service.create.application.SimulationEngine;
import com.renewsim.backend.simulation_service.create.application.command.CreateRealSimulationCommand;
import com.renewsim.backend.simulation_service.create.application.port.out.CreateSimulationRepositoryPort;
import com.renewsim.backend.simulation_service.detail.application.port.out.SimulationDetailQueryPort;
import com.renewsim.backend.simulation_service.domain.exception.InvalidSimulationStatusTransitionException;
import com.renewsim.backend.simulation_service.domain.exception.SimulationNotFoundException;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.simulation_service.shared.application.SimulationBusinessTelemetry;
import com.renewsim.backend.simulation_service.shared.application.SimulationDetailsResult;
import com.renewsim.backend.simulation_service.shared.application.SimulationTechnologySupport;
import com.renewsim.backend.simulation_service.shared.application.SimulationUseCaseTelemetry;
import com.renewsim.backend.simulation_service.update.application.command.UpdateSimulationCommand;
import com.renewsim.backend.simulation_service.update.application.port.in.UpdateSimulationUseCase;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.renewsim.backend.shared.exception.ForbiddenException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

/**
 * Use case that edits an existing simulation and recomputes its results.
 *
 * <p>Only the simulation owner or an admin may edit; terminal states reject the update with
 * {@link InvalidSimulationStatusTransitionException}. Because the {@code Simulation} aggregate
 * keeps its core attributes final, the edit rebuilds the aggregate preserving the original
 * id, scenario origin and creator, then reuses the same {@link SimulationEngine} contract as
 * creation so updated results stay comparable to the original snapshot.</p>
 *
 * <p>Like {@code CreateSimulationService}, success telemetry and business metrics are recorded
 * only after commit so rolled-back edits are never counted.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional
public class UpdateSimulationService implements UpdateSimulationUseCase {

    private static final Logger log = LoggerFactory.getLogger(UpdateSimulationService.class);
    private static final String USE_CASE = "update";

    private final SimulationDetailQueryPort detailQueryPort;
    private final CreateSimulationRepositoryPort repository;
    private final SimulationCompletionAssembler completionAssembler;
    private final SimulationUseCaseTelemetry telemetry;
    private final SimulationBusinessTelemetry businessTelemetry;
    private final SimulationTechnologySupport technologySupport;

    @Override
    public SimulationDetailsResult updateSimulation(UpdateSimulationCommand command, String requesterUsername,
            boolean isAdmin) {
        Timer.Sample sample = telemetry.start();
        try {
            Simulation existing = getEditableSimulation(command.simulationId(), requesterUsername, isAdmin);
            SimulationTechnologySupport.ResolvedTechnologyContext resolved = technologySupport.resolve(
                    command.technology(),
                    command.technologyIds());
            SimulationEngine simulationEngine = resolved.engine();
            List<Long> technologyIds = resolved.technologyIds();
            Simulation updated = toUpdatedSimulation(command, existing, technologyIds);

            SimulationDetailsResult result = simulationEngine.simulate(updated,
                    toEngineCommand(command, existing, technologyIds));
            updated.update(completionAssembler.toCompletion(result, technologyIds));
            repository.save(updated);

            recordUpdateSuccess(command, result, sample);

            return result;
        } catch (RuntimeException ex) {
            telemetry.recordError(USE_CASE, sample);
            log.info("Simulation update rejected requester={} simulationId={} energyType={} reason={}",
                    requesterUsername,
                    command.simulationId(),
                    command.technology().value(),
                    ex.getClass().getSimpleName());
            throw ex;
        }
    }

    private void recordUpdateSuccess(
            UpdateSimulationCommand command,
            SimulationDetailsResult result,
            Timer.Sample sample) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            telemetry.recordSuccess(USE_CASE, sample);
            recordBusinessMetrics(command, result);
            log.info("Simulation update succeeded simulationId={} energyType={}",
                    command.simulationId(),
                    command.technology().value());
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                telemetry.recordSuccess(USE_CASE, sample);
                recordBusinessMetrics(command, result);
                log.info("Simulation update succeeded simulationId={} energyType={}",
                        command.simulationId(),
                        command.technology().value());
            }

            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    telemetry.recordError(USE_CASE, sample);
                    log.warn("Simulation update rolled back simulationId={} energyType={}",
                            command.simulationId(),
                            command.technology().value());
                }
            }
        });
    }

    private void recordBusinessMetrics(UpdateSimulationCommand command, SimulationDetailsResult result) {
        businessTelemetry.recordRecommendation(command.technology().value(), result.summary().recommendation());
        businessTelemetry.recordAttention(command.technology().value(), result);
    }

    private Simulation getEditableSimulation(Long id, String requesterUsername, boolean isAdmin) {
        Simulation simulation = detailQueryPort.findById(id)
                .orElseThrow(() -> new SimulationNotFoundException(id));
        if (!isAdmin && !simulation.isOwnedBy(requesterUsername)) {
            throw new ForbiddenException("Not owner of simulation");
        }
        if (simulation.getStatus().isTerminal()) {
            throw new InvalidSimulationStatusTransitionException("update", simulation.getStatus());
        }
        return simulation;
    }

    private Simulation toUpdatedSimulation(UpdateSimulationCommand command, Simulation existing,
            List<Long> technologyIds) {
        return existing.rebuildForUpdate(
                command.name(),
                command.technology(),
                command.location(),
                command.system(),
                command.demand(),
                command.economics(),
                technologyIds);
    }

    private CreateRealSimulationCommand toEngineCommand(
            UpdateSimulationCommand command,
            Simulation existing,
            List<Long> technologyIds) {
        return new CreateRealSimulationCommand(
                command.name(),
                command.technology(),
                command.location(),
                command.system(),
                command.demand(),
                command.economics(),
                technologyIds,
                existing.getScenarioId(),
                existing.getCreatedBy());
    }

}
