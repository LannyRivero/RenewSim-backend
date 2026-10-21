package com.renewsim.backend.simulation_service.delete.infrastructure.adapter.out.persistence;

import com.renewsim.backend.simulation_service.delete.application.port.out.DeleteSimulationRepositoryPort;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.JpaSimulationRepository;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.SimulationRecordEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DeleteSimulationRepositoryAdapter implements DeleteSimulationRepositoryPort {

    private final JpaSimulationRepository repository;
    private final SimulationRecordEntityMapper entityMapper;

    @Override
    public Optional<Simulation> findById(Long id) {
        return repository.findById(id).map(entityMapper::toDomain);
    }

    @Override
    public List<Simulation> findActiveByCreatedBy(String createdBy) {
        return repository.findByCreatedByAndStatusNotOrderByCreatedAtDesc(createdBy, "DELETED")
                .stream()
                .map(entityMapper::toDomain)
                .toList();
    }

    @Override
    public Simulation save(Simulation simulation) {
        repository.save(entityMapper.toEntity(simulation));
        return simulation;
    }
}
