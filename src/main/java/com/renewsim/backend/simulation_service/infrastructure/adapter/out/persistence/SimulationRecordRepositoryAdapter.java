package com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.renewsim.backend.simulation_service.shared.application.port.out.SimulationRecordRepositoryPort;
import com.renewsim.backend.simulation_service.shared.application.port.out.TechnologyLookupPort;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.simulation_service.domain.model.SimulationId;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.entity.SimulationEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class SimulationRecordRepositoryAdapter implements SimulationRecordRepositoryPort {

    private final JpaSimulationRepository repository;
    private final SimulationRecordEntityMapper entityMapper;
    private final TechnologyLookupPort technologyLookupPort;

    @Autowired
    public SimulationRecordRepositoryAdapter(
            JpaSimulationRepository repository,
            ObjectMapper objectMapper,
            TechnologyLookupPort technologyLookupPort) {
        this(repository, new SimulationRecordEntityMapper(
                new SimulationInputSnapshotCodec(objectMapper)), technologyLookupPort);
    }

    SimulationRecordRepositoryAdapter(
            JpaSimulationRepository repository,
            SimulationRecordEntityMapper entityMapper,
            TechnologyLookupPort technologyLookupPort) {
        this.repository = repository;
        this.entityMapper = entityMapper;
        this.technologyLookupPort = technologyLookupPort;
    }

    @Override
    public Simulation save(Simulation simulation) {
        SimulationEntity entity = entityMapper.toEntity(simulation);
        entity.setCo2Reduction(resolvePersistedCo2Reduction(simulation));
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

    private double resolvePersistedCo2Reduction(Simulation simulation) {
        if (simulation.getAnnualGenerationKwh() == null || simulation.getTechnology() == null) {
            return 0.0;
        }
        return technologyLookupPort.findActiveCo2ReductionFactorByEnergyType(simulation.getTechnology().value())
                .map(factor -> simulation.getAnnualGenerationKwh() * factor)
                .orElse(0.0);
    }
}
