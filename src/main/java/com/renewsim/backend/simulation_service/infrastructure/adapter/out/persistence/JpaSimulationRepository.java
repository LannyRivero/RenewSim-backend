package com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.entity.SimulationEntity;

import java.time.LocalDateTime;
import java.util.List;

public interface JpaSimulationRepository extends JpaRepository<SimulationEntity, Long> {

    List<SimulationEntity> findByCreatedByOrderByCreatedAtDesc(String createdBy);

    List<SimulationEntity> findByCreatedByAndStatusNotOrderByCreatedAtDesc(String createdBy, String status);

    @Query(value = """
            select s from SimulationEntity s
            where s.createdBy = :createdBy
              and s.status <> 'DELETED'
              and (:location is null or locate(:location, s.locationSearchKey) > 0)
            """,
            countQuery = """
            select count(s) from SimulationEntity s
            where s.createdBy = :createdBy
              and s.status <> 'DELETED'
              and (:location is null or locate(:location, s.locationSearchKey) > 0)
            """)
    Page<SimulationEntity> searchHistory(
            @Param("createdBy") String createdBy,
            @Param("location") String location,
            Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from SimulationEntity s where s.status = 'DELETED' and s.updatedAt < :cutoff")
    int deleteDeletedOlderThan(@Param("cutoff") LocalDateTime cutoff);
}
