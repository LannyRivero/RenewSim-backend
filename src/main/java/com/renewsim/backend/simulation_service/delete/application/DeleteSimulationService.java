package com.renewsim.backend.simulation_service.delete.application;

import com.renewsim.backend.simulation_service.delete.application.port.in.DeleteRealSimulationUseCase;
import com.renewsim.backend.simulation_service.delete.application.port.out.DeleteSimulationRepositoryPort;
import com.renewsim.backend.simulation_service.domain.exception.SimulationNotFoundException;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.shared.exception.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class DeleteSimulationService implements DeleteRealSimulationUseCase {

    private final DeleteSimulationRepositoryPort repository;

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
            throw new ForbiddenException("Not owner of simulation");
        }
        return simulation;
    }
}
