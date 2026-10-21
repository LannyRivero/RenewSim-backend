package com.renewsim.backend.simulation_service.purge.infrastructure.adapter.out.persistence;

import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.JpaSimulationRepository;
import com.renewsim.backend.simulation_service.purge.application.port.out.PurgeDeletedSimulationRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class PurgeDeletedSimulationRepositoryAdapter implements PurgeDeletedSimulationRepositoryPort {

    private final JpaSimulationRepository repository;

    @Override
    public int purgeDeletedOlderThan(LocalDateTime cutoff) {
        return repository.deleteDeletedOlderThan(cutoff);
    }
}
