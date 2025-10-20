package com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.renewsim.backend.simulation_service.application.port.out.SimulationRepositoryPort;
import com.renewsim.backend.simulation_service.infrastructure.mapper.SimulationMapper;
import com.renewsim.backend.simulation_service.infrastructure.persistence.entity.SimulationEntity;
import com.renewsim.backend.simulation_service.domain.model.Simulation;

import java.util.List;
import java.util.Optional;

/**
 * Adapter implementing the SimulationRepositoryPort using Spring Data JPA.
 */
@Component
@RequiredArgsConstructor
public class SimulationRepositoryAdapter implements SimulationRepositoryPort {

    private final JpaSimulationRepository jpaRepository;
    private final SimulationMapper mapper;

    @Override
    public Simulation save(Simulation simulation) {
        SimulationEntity entity = mapper.toEntity(simulation);
        SimulationEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Simulation> findById(Long id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public List<Simulation> findAllByUserId(Long userId) {
        // Implementación temporal, ajustable si la entidad incorpora userId
        return mapper.toDomainList(jpaRepository.findAllById(userId));
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}
