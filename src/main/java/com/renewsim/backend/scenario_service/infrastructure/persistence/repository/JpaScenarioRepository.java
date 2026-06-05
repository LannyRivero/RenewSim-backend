package com.renewsim.backend.scenario_service.infrastructure.persistence.repository;

import com.renewsim.backend.scenario_service.infrastructure.persistence.entity.ScenarioEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JpaScenarioRepository extends JpaRepository<ScenarioEntity, Long> {

    List<ScenarioEntity> findByIsActiveTrue();
}
