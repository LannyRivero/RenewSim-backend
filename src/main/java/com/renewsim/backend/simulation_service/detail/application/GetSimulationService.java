package com.renewsim.backend.simulation_service.detail.application;

import com.renewsim.backend.simulation_service.detail.application.port.in.GetRealSimulationUseCase;
import com.renewsim.backend.simulation_service.detail.application.port.out.SimulationDetailQueryPort;
import com.renewsim.backend.simulation_service.shared.application.SimulationDetailsResult;
import com.renewsim.backend.simulation_service.shared.application.port.out.SimulationResultSnapshotReaderPort;
import com.renewsim.backend.simulation_service.domain.exception.SimulationNotFoundException;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetSimulationService implements GetRealSimulationUseCase {

    private final SimulationDetailQueryPort repository;
    private final SimulationResultSnapshotReaderPort snapshotReader;

    @Override
    public SimulationDetailsResult getSimulationById(Long id, String requesterUsername, boolean isAdmin) {
        Simulation simulation = getAccessibleSimulation(id, requesterUsername, isAdmin);
        if (!simulation.hasResult()) {
            throw new SimulationNotFoundException(id);
        }
        try {
            return snapshotReader.read(simulation.getResultSnapshot());
        } catch (IllegalStateException ex) {
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
