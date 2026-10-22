package com.renewsim.backend.simulation_service.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Immutable snapshot of one successfully completed simulation execution. */
@Entity
@Table(name = "simulation_result_revisions")
@Getter
@Setter
@NoArgsConstructor
public class SimulationResultRevisionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "simulation_id", nullable = false, updatable = false)
    private Long simulationId;

    @Column(name = "revision_number", nullable = false, updatable = false)
    private Integer revisionNumber;

    @Column(name = "input_snapshot", nullable = false, columnDefinition = "LONGTEXT", updatable = false)
    private String inputSnapshot;

    @Column(name = "result_snapshot", nullable = false, columnDefinition = "LONGTEXT", updatable = false)
    private String resultSnapshot;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
