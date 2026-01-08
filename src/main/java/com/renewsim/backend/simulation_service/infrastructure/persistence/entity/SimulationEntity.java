package com.renewsim.backend.simulation_service.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "simulations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SimulationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String location;

    @Column(name = "energy_type", nullable = false)
    private String energyType;

    @Column(name = "project_size", nullable = false)
    private double projectSize;

    @Column(nullable = false)
    private double budget;

    @Column(name = "estimated_energy", nullable = false)
    private double estimatedEnergy;

    @Column(name = "co2_reduction", nullable = false)
    private double co2Reduction;

    @Column(name = "created_by", nullable = false)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "simulation_technologies",
        joinColumns = @JoinColumn(name = "simulation_id")
    )
    @Column(name = "technology_id")
    @Builder.Default
    private List<Long> technologyIds = new ArrayList<>();
}


