package com.renewsim.backend.simulation_service.detail.infrastructure.adapter.out.persistence;

import com.renewsim.backend.simulation_service.detail.application.port.out.SimulationDetailQueryPort;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.JpaSimulationRepository;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.SimulationRecordEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SimulationDetailQueryAdapter implements SimulationDetailQueryPort {

    private final JpaSimulationRepository repository;
    private final SimulationRecordEntityMapper entityMapper;

    @Override
    public Optional<Simulation> findById(Long id) {
        return repository.findById(id).map(entityMapper::toDomain);
    }
}
