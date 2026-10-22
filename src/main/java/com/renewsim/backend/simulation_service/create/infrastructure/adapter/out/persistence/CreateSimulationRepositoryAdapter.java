package com.renewsim.backend.simulation_service.create.infrastructure.adapter.out.persistence;

import com.renewsim.backend.simulation_service.create.application.port.out.CreateSimulationRepositoryPort;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.simulation_service.domain.model.SimulationId;
import com.renewsim.backend.simulation_service.domain.model.SimulationStatus;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.JpaSimulationRepository;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.SimulationRecordEntityMapper;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.SimulationResultRevisionJpaRepository;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.entity.SimulationEntity;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.entity.SimulationResultRevisionEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class CreateSimulationRepositoryAdapter implements CreateSimulationRepositoryPort {

    private final JpaSimulationRepository repository;
    private final SimulationRecordEntityMapper entityMapper;
    private final SimulationResultRevisionJpaRepository revisionRepository;

    /**
     * Persists the simulation and, once completed, appends its immutable result revision.
     *
     * <p>The revision insert and current-revision pointer update share this transaction so a
     * failure cannot publish a partial revision or replace the previous current revision.</p>
     *
     * @param simulation aggregate state to persist
     * @return the persisted aggregate, with its generated identifier assigned when new
     */
    @Override
    @Transactional
    public Simulation save(Simulation simulation) {
        SimulationEntity entity = entityMapper.toEntity(simulation);
        boolean newSimulation = simulation.getId() == null;
        boolean appendRevision = simulation.getStatus() == SimulationStatus.COMPLETED
                && simulation.hasResult()
                && entity.getInputSnapshot() != null;

        if (appendRevision && !newSimulation) {
            SimulationEntity existing = repository.findByIdForUpdate(simulation.getId().value())
                    .orElseThrow(() -> new IllegalStateException("Simulation disappeared during revision save"));
            // The relationship is intentionally read-only; preserve it during merge to avoid
            // Hibernate treating the unmapped value on this new entity as a relationship change.
            entity.setCurrentRevision(existing.getCurrentRevision());
        }

        SimulationEntity saved = repository.save(entity);
        if (simulation.getId() == null) {
            simulation.assignId(SimulationId.of(saved.getId()));
        }

        if (appendRevision) {
            Long simulationId = saved.getId();
            if (newSimulation) {
                repository.findByIdForUpdate(simulationId)
                        .orElseThrow(() -> new IllegalStateException("Simulation disappeared during revision save"));
            }

            SimulationResultRevisionEntity revision = new SimulationResultRevisionEntity();
            revision.setSimulationId(simulationId);
            revision.setRevisionNumber(revisionRepository.findHighestRevisionNumber(simulationId) + 1);
            revision.setInputSnapshot(saved.getInputSnapshot());
            revision.setResultSnapshot(saved.getResultSnapshot());
            revision.setCreatedAt(LocalDateTime.now());
            SimulationResultRevisionEntity persistedRevision = revisionRepository.saveAndFlush(revision);

            if (repository.updateCurrentRevisionId(simulationId, persistedRevision.getId()) != 1) {
                throw new IllegalStateException("Unable to advance current simulation revision");
            }
        }
        return simulation;
    }
}
