package com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.renewsim.backend.simulation_service.infrastructure.persistence.entity.SimulationEntity;

import java.util.List;

public interface JpaSimulationRepository extends JpaRepository<SimulationEntity, Long> {

    List<SimulationEntity> findByCreatedByOrderByCreatedAtDesc(String createdBy);
    java.util.Optional<SimulationEntity> findFirstByCreatedByAndNameAndEnergyTypeAndLocationLatAndLocationLng(
            String createdBy,
            String name,
            String energyType,
            Double locationLat,
            Double locationLng);
    
    void deleteAllByCreatedBy(String createdBy);
}
