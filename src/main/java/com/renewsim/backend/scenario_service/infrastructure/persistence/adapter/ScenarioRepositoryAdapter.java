package com.renewsim.backend.scenario_service.infrastructure.persistence.adapter;

import com.renewsim.backend.scenario_service.application.port.out.ScenarioRepositoryPort;
import com.renewsim.backend.scenario_service.domain.model.Scenario;
import com.renewsim.backend.scenario_service.infrastructure.mapper.ScenarioMapper;
import com.renewsim.backend.scenario_service.infrastructure.persistence.repository.JpaScenarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ScenarioRepositoryAdapter implements ScenarioRepositoryPort {

    private final JpaScenarioRepository jpaRepository;
    private final ScenarioMapper mapper;

    @Override
    public Scenario save(Scenario scenario) {
        var entity = mapper.toEntity(scenario);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Scenario> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Scenario> findAllActive() {
        return jpaRepository.findByIsActiveTrue().stream().map(mapper::toDomain).toList();
    }
}
