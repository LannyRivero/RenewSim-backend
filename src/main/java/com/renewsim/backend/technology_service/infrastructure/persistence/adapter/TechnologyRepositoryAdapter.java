package com.renewsim.backend.technology_service.infrastructure.persistence.adapter;

import com.renewsim.backend.technology_service.application.port.out.TechnologyRepositoryPort;
import com.renewsim.backend.technology_service.domain.model.Technology;
import com.renewsim.backend.technology_service.domain.model.vo.EnergyType;
import com.renewsim.backend.technology_service.infrastructure.mapper.TechnologyMapper;
import com.renewsim.backend.technology_service.infrastructure.persistence.entity.TechnologyEntity;
import com.renewsim.backend.technology_service.infrastructure.persistence.repository.JpaTechnologyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TechnologyRepositoryAdapter implements TechnologyRepositoryPort {

    private final JpaTechnologyRepository jpaRepository;
    private final TechnologyMapper mapper;

    @Override
    public Technology save(Technology technology) {
        var entity = mapper.toEntity(technology);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public Optional<Technology> findById(Long id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }
     @Override
    public Optional<Technology> findByName(String name) { 
        return jpaRepository.findByName(name).map(mapper::toDomain);
    }

    @Override
    public List<Technology> findAll() {
        return jpaRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public Page<Technology> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(mapper::toDomain);
    }

    @Override
    public Page<Technology> findByEnergyType(EnergyType energyType, Pageable pageable) {
        return jpaRepository.findByEnergyType(toEntityEnergyType(energyType), pageable).map(mapper::toDomain);
    }

    @Override
    public Page<Technology> findActiveByEnergyType(EnergyType energyType, Pageable pageable) {
        return jpaRepository.findByEnergyTypeAndIsActiveTrue(toEntityEnergyType(energyType), pageable)
                .map(mapper::toDomain);
    }

    @Override
    public Page<Technology> findAllActive(Pageable pageable) {
        return jpaRepository.findByIsActiveTrue(pageable).map(mapper::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByName(String name) {
        return jpaRepository.existsByName(name);
    }

    private TechnologyEntity.EnergyType toEntityEnergyType(EnergyType energyType) {
        return switch (energyType) {
            case SOLAR -> TechnologyEntity.EnergyType.SOLAR;
            case WIND, EOLIC -> TechnologyEntity.EnergyType.WIND;
            case HYDRO -> TechnologyEntity.EnergyType.HYDRO;
            case GEOTHERMAL -> TechnologyEntity.EnergyType.GEOTHERMAL;
            case BIOMASS -> TechnologyEntity.EnergyType.BIOMASS;
        };
    }
}

