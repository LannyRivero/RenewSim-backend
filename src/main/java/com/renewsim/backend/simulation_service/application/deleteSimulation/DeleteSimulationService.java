package com.renewsim.backend.simulation_service.application.deleteSimulation;

import com.renewsim.backend.simulation_service.application.port.out.SimulationRecordRepositoryPort;
import com.renewsim.backend.simulation_service.domain.exception.SimulationNotFoundException;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DeleteSimulationService implements DeleteRealSimulationUseCase {

    private final SimulationRecordRepositoryPort repository;

    @Override
    public void deleteSimulation(Long id, String requesterUsername, boolean isAdmin) {
        Simulation simulation = getAccessibleSimulation(id, requesterUsername, isAdmin);
        simulation.delete();
        repository.deleteById(simulation.getId().value());
    }

    @Override
    public void deleteAllUserSimulations(String username) {
        repository.deleteAllByCreatedBy(username);
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
