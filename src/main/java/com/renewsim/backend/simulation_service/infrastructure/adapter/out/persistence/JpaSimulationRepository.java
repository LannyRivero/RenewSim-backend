package com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.entity.SimulationEntity;

import java.util.List;

public interface JpaSimulationRepository extends JpaRepository<SimulationEntity, Long> {

    List<SimulationEntity> findByCreatedByOrderByCreatedAtDesc(String createdBy);

    void deleteAllByCreatedBy(String createdBy);
}
