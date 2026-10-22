package com.renewsim.backend.simulation_service.detail.infrastructure.adapter.out.persistence;

import com.renewsim.backend.simulation_service.detail.application.port.out.SimulationDetailQueryPort;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.JpaSimulationRepository;
import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.SimulationRecordEntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SimulationDetailQueryAdapter implements SimulationDetailQueryPort {

    private final JpaSimulationRepository repository;
    private final SimulationRecordEntityMapper entityMapper;

    /**
     * Loads a simulation and maps its current result while the lazy revision is
     * available.
     *
     * @param id simulation identifier
     * @return the matching simulation, if present
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Simulation> findById(Long id) {
        return repository.findById(id).map(entityMapper::toDomain);
    }
}
