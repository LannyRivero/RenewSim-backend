package com.renewsim.backend.technology_service.infrastructure.persistence.repository;

import com.renewsim.backend.technology_service.infrastructure.persistence.entity.TechnologyEntity;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaTechnologyRepository extends JpaRepository<TechnologyEntity, Long> {

    boolean existsByName(String name);
    
    Optional<TechnologyEntity> findByName(String name);

    Page<TechnologyEntity> findByEnergyType(TechnologyEntity.EnergyType energyType, Pageable pageable);

    Page<TechnologyEntity> findByEnergyTypeAndIsActiveTrue(TechnologyEntity.EnergyType energyType, Pageable pageable);

    Page<TechnologyEntity> findByIsActiveTrue(Pageable pageable);

    Page<TechnologyEntity> findByIsActiveTrueAndNameContainingIgnoreCase(String name, Pageable pageable);

    Page<TechnologyEntity> findByEnergyTypeAndIsActiveTrueAndNameContainingIgnoreCase(TechnologyEntity.EnergyType energyType, String name, Pageable pageable);
}
