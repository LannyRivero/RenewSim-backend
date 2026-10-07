package com.renewsim.backend.simulation_service.delete.application;

import com.renewsim.backend.simulation_service.delete.application.port.in.DeleteRealSimulationUseCase;
import com.renewsim.backend.simulation_service.delete.application.port.out.DeleteSimulationRepositoryPort;
import com.renewsim.backend.simulation_service.domain.exception.SimulationNotFoundException;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.shared.exception.ForbiddenException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class DeleteSimulationService implements DeleteRealSimulationUseCase {

    private final DeleteSimulationRepositoryPort repository;
    private final Clock clock;

    @Override
    public void deleteSimulation(Long id, String requesterUsername, boolean isAdmin) {
        Simulation simulation = getAccessibleSimulation(id, requesterUsername, isAdmin);
        simulation.delete(deletionTimestamp());
        repository.save(simulation);
    }

    @Override
    public void deleteAllUserSimulations(String username) {
        repository.findActiveByCreatedBy(username).forEach(simulation -> {
            simulation.delete(deletionTimestamp());
            repository.save(simulation);
        });
    }

    private LocalDateTime deletionTimestamp() {
        return LocalDateTime.now(clock);
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
