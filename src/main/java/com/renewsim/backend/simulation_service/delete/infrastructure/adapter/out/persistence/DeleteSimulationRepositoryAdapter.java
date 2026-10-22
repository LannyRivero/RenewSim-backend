package com.renewsim.backend.simulation_service.delete.infrastructure.adapter.out.persistence;

import com.renewsim.backend.simulation_service.delete.application.port.out.DeleteSimulationRepositoryPort;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.JpaSimulationRepository;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.SimulationRecordEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DeleteSimulationRepositoryAdapter implements DeleteSimulationRepositoryPort {

    private final JpaSimulationRepository repository;
    private final SimulationRecordEntityMapper entityMapper;

    /**
     * Loads one simulation and maps its current result revision within the
     * persistence context.
     *
     * @param id simulation identifier
     * @return the simulation when it exists
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Simulation> findById(Long id) {
        return repository.findById(id).map(entityMapper::toDomain);
    }

    /**
     * Loads and maps the owner's simulations while the persistence context remains
     * open.
     *
     * <p>
     * Domain mapping may resolve the lazy current-revision association. This is a
     * read-only
     * transaction because this operation only queries and maps existing
     * simulations.
     * </p>
     *
     * @param createdBy owner whose non-deleted simulations are requested
     * @return the owner's non-deleted simulations
     */
    @Override
    @Transactional(readOnly = true)
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
