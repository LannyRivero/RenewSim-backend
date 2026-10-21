package com.renewsim.backend.simulation_service.create.infrastructure.adapter.out.persistence;

import com.renewsim.backend.simulation_service.create.application.port.out.CreateSimulationRepositoryPort;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.simulation_service.domain.model.SimulationId;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.JpaSimulationRepository;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.SimulationRecordEntityMapper;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.entity.SimulationEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CreateSimulationRepositoryAdapter implements CreateSimulationRepositoryPort {

    private final JpaSimulationRepository repository;
    private final SimulationRecordEntityMapper entityMapper;

    @Override
    public Simulation save(Simulation simulation) {
        SimulationEntity entity = entityMapper.toEntity(simulation);
        SimulationEntity saved = repository.save(entity);
        if (simulation.getId() == null) {
            simulation.assignId(SimulationId.of(saved.getId()));
        }
        return simulation;
    }
}
