package com.renewsim.backend.simulation_service.detail.application;

import com.renewsim.backend.simulation_service.detail.application.port.in.GetRealSimulationUseCase;
import com.renewsim.backend.simulation_service.detail.application.port.out.SimulationDetailQueryPort;
import com.renewsim.backend.simulation_service.domain.exception.SimulationNotFoundException;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.simulation_service.shared.application.SimulationDetailsResult;
import com.renewsim.backend.simulation_service.shared.application.SimulationUseCaseTelemetry;
import com.renewsim.backend.simulation_service.shared.application.port.out.SimulationResultSnapshotReaderPort;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetSimulationService implements GetRealSimulationUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetSimulationService.class);
    private static final String USE_CASE = "detail";

    private final SimulationDetailQueryPort repository;
    private final SimulationResultSnapshotReaderPort snapshotReader;
    private final SimulationUseCaseTelemetry telemetry;

    @Override
    public SimulationDetailsResult getSimulationById(Long id, String requesterUsername, boolean isAdmin) {
        Timer.Sample sample = telemetry.start();
        try {
            Simulation simulation = getAccessibleSimulation(id, requesterUsername, isAdmin);
            if (!simulation.hasResult()) {
                throw new SimulationNotFoundException(id);
            }

            SimulationDetailsResult result = readSnapshotOrDegrade(id, simulation.getResultSnapshot(), requesterUsername, isAdmin, sample);
            telemetry.recordSuccess(USE_CASE, sample);
            log.info("Simulation detail retrieved requester={} simulationId={} admin={}", requesterUsername, id, isAdmin);
            return result;
        } catch (RuntimeException ex) {
            telemetry.recordError(USE_CASE, sample);
            log.info("Simulation detail rejected requester={} simulationId={} admin={} reason={}",
                    requesterUsername,
                    id,
                    isAdmin,
                    ex.getClass().getSimpleName());
            throw ex;
        }
    }

    private SimulationDetailsResult readSnapshotOrDegrade(
            Long id,
            String snapshot,
            String requesterUsername,
            boolean isAdmin,
            Timer.Sample sample) {
        try {
            return snapshotReader.read(snapshot);
        } catch (IllegalStateException ex) {
            telemetry.recordDegraded(USE_CASE, sample);
            telemetry.recordSnapshotDegraded("invalid_result_snapshot");
            log.warn("Simulation detail snapshot degraded requester={} simulationId={} admin={} reason={}",
                    requesterUsername,
                    id,
                    isAdmin,
                    ex.getClass().getSimpleName());
            throw new SimulationNotFoundException(id);
        }
    }

    private Simulation getAccessibleSimulation(Long id, String requesterUsername, boolean isAdmin) {
        Simulation simulation = repository.findById(id)
                .orElseThrow(() -> new SimulationNotFoundException(id));
        if (!isAdmin && !simulation.isOwnedBy(requesterUsername)) {
            throw new AccessDeniedException("Not owner of simulation");
        }
        return simulation;
    }
}
