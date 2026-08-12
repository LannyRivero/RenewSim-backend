package com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.simulation_service.create.application.port.out.CreateSimulationRepositoryPort;
import com.renewsim.backend.simulation_service.dashboard.application.port.out.PortfolioDashboardQueryPort;
import com.renewsim.backend.simulation_service.delete.application.port.out.DeleteSimulationRepositoryPort;
import com.renewsim.backend.simulation_service.detail.application.port.out.SimulationDetailQueryPort;
import com.renewsim.backend.simulation_service.history.application.port.out.SimulationHistoryQueryPort;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.simulation_service.domain.model.SimulationId;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.entity.SimulationEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SimulationRecordRepositoryAdapter
        implements CreateSimulationRepositoryPort, SimulationDetailQueryPort, SimulationHistoryQueryPort,
        DeleteSimulationRepositoryPort, PortfolioDashboardQueryPort {

    private final JpaSimulationRepository repository;
    private final SimulationRecordEntityMapper entityMapper;

    @Autowired
    public SimulationRecordRepositoryAdapter(
            JpaSimulationRepository repository,
            ObjectMapper objectMapper) {
        this(repository, new SimulationRecordEntityMapper(
                new SimulationInputSnapshotCodec(objectMapper)));
    }

    SimulationRecordRepositoryAdapter(
            JpaSimulationRepository repository,
            SimulationRecordEntityMapper entityMapper) {
        this.repository = repository;
        this.entityMapper = entityMapper;
    }

    @Override
    public Simulation save(Simulation simulation) {
        SimulationEntity entity = entityMapper.toEntity(simulation);
        SimulationEntity saved = repository.save(entity);
        if (simulation.getId() == null) {
            simulation.assignId(SimulationId.of(saved.getId()));
        }
        return simulation;
    }

    @Override
    public Optional<Simulation> findById(Long id) {
        return repository.findById(id).map(entityMapper::toDomain);
    }

    @Override
    public List<Simulation> findByCreatedByOrderByCreatedAtDesc(String createdBy) {
        return repository.findByCreatedByOrderByCreatedAtDesc(createdBy)
                .stream()
                .map(entityMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public void deleteAllByCreatedBy(String createdBy) {
        repository.deleteAllByCreatedBy(createdBy);
    }
}
