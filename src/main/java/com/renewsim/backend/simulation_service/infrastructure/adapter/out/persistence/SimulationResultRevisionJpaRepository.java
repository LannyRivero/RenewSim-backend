package com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence;

import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.entity.SimulationResultRevisionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SimulationResultRevisionJpaRepository extends JpaRepository<SimulationResultRevisionEntity, Long> {

    @Query("select coalesce(max(revision.revisionNumber), 0) from SimulationResultRevisionEntity revision "
            + "where revision.simulationId = :simulationId")
    int findHighestRevisionNumber(@Param("simulationId") Long simulationId);
}
