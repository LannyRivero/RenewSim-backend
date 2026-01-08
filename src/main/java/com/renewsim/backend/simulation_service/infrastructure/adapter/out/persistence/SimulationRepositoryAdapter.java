package com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import com.renewsim.backend.simulation_service.application.port.out.SimulationRepositoryPort;
import com.renewsim.backend.simulation_service.domain.model.Simulation;
import com.renewsim.backend.simulation_service.infrastructure.mapper.SimulationMapper;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SimulationRepositoryAdapter implements SimulationRepositoryPort {

    private final JpaSimulationRepository repository;
    private final SimulationMapper mapper;

    @Override
    public Simulation save(Simulation simulation) {
        return mapper.toDomain(
                repository.save(mapper.toEntity(simulation)));
    }

    @Override
    public List<Simulation> findAllByCreatedBy(String username) {
        return repository.findByCreatedByOrderByCreatedAtDesc(username)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Simulation> findById(Long id) {
        return repository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
